package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentStage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionGenerationAggregate;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionGenerationTraceEntryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.persistence.QuestionPersistenceFacade;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.registry.SkillRegistry;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.trace.QuestionGenerationTracePayloads;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ExamConstraintTool;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        return run(request, userId, requestId, null, null);
    }

    public QuestionGenerationAggregate run(QuestionGenerateRequestDTO request,
                                           UUID userId,
                                           String requestId,
                                           Consumer<QuestionAgentStage> stageListener) {
        return run(request, userId, requestId, stageListener, null);
    }

    public QuestionGenerationAggregate run(QuestionGenerateRequestDTO request,
                                           UUID userId,
                                           String requestId,
                                           Consumer<QuestionAgentStage> stageListener,
                                           Consumer<QuestionGenerationTraceEntryDTO> traceListener) {
        QuestionAgentContext context = new QuestionAgentContext();
        context.setRequest(request);
        context.setUserId(userId);
        context.setSessionId(request != null ? request.getSessionId() : null);
        context.setRequestId(requestId);
        context.setTraceEntryListener(traceListener);
        context.setStage(QuestionAgentStage.RECEIVED);
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.CONTEXT_READY);
        collectContext(context);
        context.appendTraceEntry(
                "orchestrator",
                "context_summary",
                context.localize("上下文资料已整合", "Context gathered"),
                context.localize(
                        "已为本次生成整合 " + context.getEvidences().size() + " 条参考资料。",
                        "Collected " + context.getEvidences().size() + " reference items for this generation."
                ),
                buildContextSummaryPayload(context.getEvidences())
        );
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.PLANNED);
        context.setBlueprint(skillRegistry.getPaperPlanningSkill().execute(context));
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.GENERATED);
        List<QuestionDraftDTO> drafts = mutableList(skillRegistry.getQuestionGenerationSkill().execute(context));
        context.setDrafts(drafts);
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.VALIDATED);
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
        context.appendTraceEntry(
                "orchestrator",
                "validation",
                context.localize("草稿校验结果", "Draft validation result"),
                resolveValidationSummary(context, normalizedDrafts.size(), context.getIssues()),
                QuestionGenerationTracePayloads.validation(normalizedDrafts, context.getIssues())
        );
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.REPAIRED);
        List<QuestionResponseDTO> finalQuestions = mutableList(skillRegistry.getPaperReviewSkill().execute(context));
        notifyStage(context, stageListener);

        context.setStage(QuestionAgentStage.ASSEMBLED);
        examConstraintTool.rebalanceQuestionScores(finalQuestions, context.getRequest());
        context.setFinalQuestions(finalQuestions);
        context.setIssues(mutableList(examConstraintTool.validate(finalQuestions, context.getRequest(), referenceSignatures)));
        context.appendTraceEntry(
                "orchestrator",
                "final_questions",
                context.localize("最终试卷已组装", "Final paper assembled"),
                context.localize(
                        "已组装 " + finalQuestions.size() + " 道最终题目，剩余 "
                                + context.getIssues().size() + " 个待关注问题。",
                        "Assembled " + finalQuestions.size() + " final questions with "
                                + context.getIssues().size() + " remaining issues to review."
                ),
                QuestionGenerationTracePayloads.finalQuestions(finalQuestions, context.getIssues())
        );
        notifyStage(context, stageListener);

        questionPersistenceFacade.saveToQuestionBank(context);

        QuestionGenerationAggregate aggregate = new QuestionGenerationAggregate();
        aggregate.setRequestId(context.getRequestId());
        aggregate.setSessionId(context.getSessionId());
        aggregate.setBlueprint(context.getBlueprint());
        aggregate.setDrafts(mutableList(context.getDrafts()));
        aggregate.setFinalQuestions(mutableList(context.getFinalQuestions()));
        aggregate.setIssues(mutableList(context.getIssues()));
        aggregate.setTraceEntries(mutableList(context.getTraceEntries()));
        aggregate.setStage(context.getStage());
        return aggregate;
    }

    private void notifyStage(QuestionAgentContext context, Consumer<QuestionAgentStage> stageListener) {
        if (stageListener != null) {
            stageListener.accept(context.getStage());
        }
    }

    private void collectContext(QuestionAgentContext context) {
        List<AgentEvidenceDTO> merged = new ArrayList<>();
        addAllIfPresent(merged, toolRegistry.getKnowledgeSearchTool().search(context));
        addAllIfPresent(merged, toolRegistry.getQuestionBankTool().loadEvidence(context));
        addAllIfPresent(merged, toolRegistry.getFileContextTool().loadEvidence(context));
        addAllIfPresent(merged, skillRegistry.getOpenTdbQuestionSkill().execute(context));
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

    private Map<String, Object> buildContextSummaryPayload(List<AgentEvidenceDTO> evidences) {
        Map<String, Integer> countsBySource = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(evidences)) {
            for (AgentEvidenceDTO evidence : evidences) {
                if (evidence == null) {
                    continue;
                }
                String source = evidence.getSourceType() == null ? "unknown" : evidence.getSourceType();
                countsBySource.put(source, countsBySource.getOrDefault(source, 0) + 1);
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("evidenceCount", evidences != null ? evidences.size() : 0);
        payload.put("countsBySource", countsBySource);
        payload.put("evidences", QuestionGenerationTracePayloads.summarizeEvidences(evidences, 6));
        return payload;
    }

    private String resolveValidationSummary(QuestionAgentContext context, int questionCount, List<?> issues) {
        int issueCount = issues != null ? issues.size() : 0;
        if (issueCount == 0) {
            return context.localize(
                    "已校验 " + questionCount + " 道草稿题目，未发现问题。",
                    "Validated " + questionCount + " draft questions with no issues found."
            );
        }
        return context.localize(
                "已校验 " + questionCount + " 道草稿题目，发现 " + issueCount + " 个问题。",
                "Validated " + questionCount + " draft questions and found " + issueCount + " issues."
        );
    }
}
