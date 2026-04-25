package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentStage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionGenerationAggregate;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.persistence.QuestionPersistenceFacade;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.registry.SkillRegistry;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ExamConstraintTool;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Runs the multi-step local agent workflow for AI question generation.
 */
@Service
@RequiredArgsConstructor
public class QuestionAgentOrchestrator {

    private final SkillRegistry skillRegistry;
    private final ToolRegistry toolRegistry;
    private final QuestionPersistenceFacade questionPersistenceFacade;

    public QuestionGenerationAggregate run(QuestionGenerateRequestDTO request, UUID userId, String requestId) {
        return run(request, userId, requestId, null);
    }

    public QuestionGenerationAggregate run(QuestionGenerateRequestDTO request,
                                           UUID userId,
                                           String requestId,
                                           Consumer<QuestionAgentStage> stageListener) {
        QuestionAgentContext context = new QuestionAgentContext();
        context.setRequest(request);
        context.setUserId(userId);
        context.setSessionId(request != null ? request.getSessionId() : null);
        context.setRequestId(requestId);
        advanceStage(context, QuestionAgentStage.RECEIVED, stageListener);

        advanceStage(context, QuestionAgentStage.CONTEXT_READY, stageListener);
        collectContext(context);

        advanceStage(context, QuestionAgentStage.PLANNED, stageListener);
        context.setBlueprint(skillRegistry.getPaperPlanningSkill().execute(context));

        advanceStage(context, QuestionAgentStage.GENERATED, stageListener);
        List<QuestionDraftDTO> drafts = mutableList(skillRegistry.getQuestionGenerationSkill().execute(context));
        context.setDrafts(drafts);

        advanceStage(context, QuestionAgentStage.VALIDATED, stageListener);
        ExamConstraintTool examConstraintTool = toolRegistry.getExamConstraintTool();
        Set<String> referenceSignatures = examConstraintTool.collectQuestionSampleSignatures(context.getEvidences());
        if (referenceSignatures == null) {
            referenceSignatures = Set.of();
        }
        List<QuestionResponseDTO> normalizedDrafts = mutableList(examConstraintTool.normalizeQuestions(
                context.getDraftQuestions(),
                context.getRequest(),
                context.getUserId(),
                context.getRequestId()
        ));
        examConstraintTool.rebalanceQuestionScores(normalizedDrafts, context.getRequest());
        context.setIssues(mutableList(examConstraintTool.validate(normalizedDrafts, context.getRequest(), referenceSignatures)));

        advanceStage(context, QuestionAgentStage.REPAIRED, stageListener);
        List<QuestionResponseDTO> finalQuestions = mutableList(skillRegistry.getPaperReviewSkill().execute(context));

        advanceStage(context, QuestionAgentStage.ASSEMBLED, stageListener);
        examConstraintTool.rebalanceQuestionScores(finalQuestions, context.getRequest());
        context.setFinalQuestions(finalQuestions);
        context.setIssues(mutableList(examConstraintTool.validate(finalQuestions, context.getRequest(), referenceSignatures)));

        questionPersistenceFacade.saveToQuestionBank(context);

        QuestionGenerationAggregate aggregate = new QuestionGenerationAggregate();
        aggregate.setRequestId(context.getRequestId());
        aggregate.setSessionId(context.getSessionId());
        aggregate.setBlueprint(context.getBlueprint());
        aggregate.setDrafts(mutableList(context.getDrafts()));
        aggregate.setFinalQuestions(mutableList(context.getFinalQuestions()));
        aggregate.setIssues(mutableList(context.getIssues()));
        aggregate.setStage(context.getStage());
        return aggregate;
    }

    private void advanceStage(QuestionAgentContext context,
                              QuestionAgentStage stage,
                              Consumer<QuestionAgentStage> stageListener) {
        context.setStage(stage);
        if (stageListener != null) {
            stageListener.accept(stage);
        }
    }

    private void collectContext(QuestionAgentContext context) {
        List<AgentEvidenceDTO> merged = new ArrayList<>();
        addAllIfPresent(merged, toolRegistry.getKnowledgeSearchTool().search(context));
        addAllIfPresent(merged, toolRegistry.getQuestionBankTool().loadEvidence(context));
        addAllIfPresent(merged, toolRegistry.getFileContextTool().loadEvidence(context));
        context.setEvidences(deduplicateEvidence(merged));
    }

    private void addAllIfPresent(List<AgentEvidenceDTO> target, List<AgentEvidenceDTO> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        target.addAll(source);
    }

    private List<AgentEvidenceDTO> deduplicateEvidence(List<AgentEvidenceDTO> evidences) {
        Map<String, AgentEvidenceDTO> deduped = new LinkedHashMap<>();
        for (AgentEvidenceDTO evidence : evidences) {
            if (evidence == null) {
                continue;
            }
            String key = (evidence.getSourceType() == null ? "" : evidence.getSourceType())
                    + '|'
                    + (evidence.getSourceId() == null ? "" : evidence.getSourceId())
                    + '|'
                    + (evidence.getTitle() == null ? "" : evidence.getTitle());
            deduped.putIfAbsent(key, evidence);
        }
        return new ArrayList<>(deduped.values());
    }

    private <T> List<T> mutableList(List<T> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }
}
