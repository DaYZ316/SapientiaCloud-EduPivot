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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

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
        QuestionAgentContext context = new QuestionAgentContext();
        context.setRequest(request);
        context.setUserId(userId);
        context.setSessionId(request != null ? request.getSessionId() : null);
        context.setRequestId(requestId);
        context.setStage(QuestionAgentStage.RECEIVED);

        collectContext(context);
        context.setStage(QuestionAgentStage.CONTEXT_READY);

        context.setBlueprint(skillRegistry.getPaperPlanningSkill().execute(context));
        context.setStage(QuestionAgentStage.PLANNED);

        List<QuestionDraftDTO> drafts = skillRegistry.getQuestionGenerationSkill().execute(context);
        context.setDrafts(drafts);
        context.setStage(QuestionAgentStage.GENERATED);

        ExamConstraintTool examConstraintTool = toolRegistry.getExamConstraintTool();
        Set<String> referenceSignatures = collectReferenceSignatures(context, examConstraintTool);
        List<QuestionResponseDTO> normalizedDrafts = examConstraintTool.normalizeQuestions(
                context.getDraftQuestions(),
                context.getRequest(),
                context.getUserId(),
                context.getRequestId()
        );
        examConstraintTool.rebalanceQuestionScores(normalizedDrafts, context.getRequest());
        context.setIssues(examConstraintTool.validate(normalizedDrafts, context.getRequest(), referenceSignatures));
        context.setStage(QuestionAgentStage.VALIDATED);

        List<QuestionResponseDTO> finalQuestions = skillRegistry.getPaperReviewSkill().execute(context);
        examConstraintTool.rebalanceQuestionScores(finalQuestions, context.getRequest());
        context.setFinalQuestions(finalQuestions);
        context.setIssues(examConstraintTool.validate(finalQuestions, context.getRequest(), referenceSignatures));
        context.setStage(QuestionAgentStage.ASSEMBLED);

        questionPersistenceFacade.saveToQuestionBank(context);

        QuestionGenerationAggregate aggregate = new QuestionGenerationAggregate();
        aggregate.setRequestId(context.getRequestId());
        aggregate.setSessionId(context.getSessionId());
        aggregate.setBlueprint(context.getBlueprint());
        aggregate.setDrafts(context.getDrafts());
        aggregate.setFinalQuestions(context.getFinalQuestions());
        aggregate.setIssues(context.getIssues());
        aggregate.setStage(context.getStage());
        return aggregate;
    }

    private void collectContext(QuestionAgentContext context) {
        List<AgentEvidenceDTO> merged = new ArrayList<>();
        merged.addAll(toolRegistry.getKnowledgeSearchTool().search(context));
        merged.addAll(toolRegistry.getQuestionBankTool().loadEvidence(context));
        merged.addAll(toolRegistry.getFileContextTool().loadEvidence(context));
        context.setEvidences(deduplicateEvidence(merged));
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

    private Set<String> collectReferenceSignatures(QuestionAgentContext context, ExamConstraintTool examConstraintTool) {
        Set<String> signatures = new LinkedHashSet<>();
        if (context == null || CollectionUtils.isEmpty(context.getEvidences())) {
            return signatures;
        }

        for (AgentEvidenceDTO evidence : context.getEvidences()) {
            if (evidence == null || !"question_sample".equalsIgnoreCase(evidence.getSourceType())) {
                continue;
            }
            String signature = examConstraintTool.buildSignature(evidence.getTitle(), evidence.getExcerpt());
            if (StringUtils.hasText(signature)) {
                signatures.add(signature);
            }
        }
        return signatures;
    }
}
