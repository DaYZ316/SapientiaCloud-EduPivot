package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGeneratePayload;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRecord;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.assembler.QuestionResponseAssembler;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ExamConstraintTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * Repairs invalid drafts with deterministic normalization first and up to two model-based repair passes if needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaperReviewSkill implements AgentSkill<List<QuestionResponseDTO>> {

    private static final int MAX_REPAIR_ATTEMPTS = 2;

    private final ChatClient chatClient;
    private final QuestionResponseAssembler questionResponseAssembler;
    private final ExamConstraintTool examConstraintTool;

    @Override
    public String name() {
        return "paperReview";
    }

    @Override
    public List<QuestionResponseDTO> execute(QuestionAgentContext context) {
        Set<String> blockedSignatures = examConstraintTool.collectQuestionSampleSignatures(context.getEvidences());
        List<QuestionResponseDTO> normalized = examConstraintTool.normalizeQuestions(
                context.getDraftQuestions(),
                context.getRequest(),
                context.getUserId(),
                context.getRequestId()
        );
        examConstraintTool.rebalanceQuestionScores(normalized, context.getRequest());
        examConstraintTool.rebalanceEstimatedTimes(normalized, resolveTargetTotalEstimatedTime(context));

        List<ValidationIssueDTO> issues = examConstraintTool.validate(normalized, context.getRequest(), blockedSignatures);
        context.setIssues(issues);

        if (!examConstraintTool.hasBlockingIssues(issues)) {
            return normalized;
        }

        List<QuestionResponseDTO> currentQuestions = normalized;
        List<ValidationIssueDTO> currentIssues = issues;
        List<QuestionResponseDTO> bestQuestions = normalized;
        List<ValidationIssueDTO> bestIssues = issues;

        for (int attemptNo = 1; attemptNo <= MAX_REPAIR_ATTEMPTS && examConstraintTool.hasBlockingIssues(currentIssues); attemptNo++) {
            try {
                String prompt = buildRepairPrompt(context, currentQuestions, currentIssues, attemptNo);
                QuestionGeneratePayload payload = chatClient
                        .prompt(new Prompt(prompt))
                        .call()
                        .entity(QuestionGeneratePayload.class);
                List<QuestionGenerateRecord> repairedRecords = payload != null ? payload.questionsOrEmpty() : List.of();

                List<QuestionResponseDTO> repaired = questionResponseAssembler.fromRecords(
                        repairedRecords,
                        context.getUserId(),
                        context.getRequestId()
                );
                List<QuestionResponseDTO> normalizedRepaired = examConstraintTool.normalizeQuestions(
                        repaired,
                        context.getRequest(),
                        context.getUserId(),
                        context.getRequestId()
                );
                examConstraintTool.rebalanceQuestionScores(normalizedRepaired, context.getRequest());
                examConstraintTool.rebalanceEstimatedTimes(normalizedRepaired, resolveTargetTotalEstimatedTime(context));
                List<ValidationIssueDTO> repairedIssues = examConstraintTool.validate(
                        normalizedRepaired,
                        context.getRequest(),
                        blockedSignatures
                );

                if (isBetterCandidate(normalizedRepaired, repairedIssues, bestQuestions, bestIssues, context)) {
                    bestQuestions = normalizedRepaired;
                    bestIssues = repairedIssues;
                }

                currentQuestions = normalizedRepaired;
                currentIssues = repairedIssues;
            } catch (Exception e) {
                log.warn("Question repair pass failed, falling back to best effort result. requestId={}, attemptNo={}, error={}",
                        context.getRequestId(), attemptNo, e.getMessage());
                break;
            }
        }

        context.setIssues(bestIssues);
        return CollectionUtils.isEmpty(bestQuestions) ? normalized : bestQuestions;
    }

    private String buildRepairPrompt(QuestionAgentContext context,
                                     List<QuestionResponseDTO> currentQuestions,
                                     List<ValidationIssueDTO> issues,
                                     int attemptNo) {
        StringBuilder prompt = new StringBuilder(QuestionConstants.QUESTION_SYSTEM_PROMPT);
        prompt.append("\n\nCurrent task: repair a generated question set.");
        prompt.append("\nReturn a JSON object matching QuestionGeneratePayload and nothing else.");
        prompt.append("\nUse this exact top-level shape: {\"questions\":[...]}.");
        prompt.append("\nThe questions array inside that object must contain exactly ")
                .append(context.getRequest().getQuestionCount())
                .append(" questions.");
        prompt.append("\nFix structural problems while preserving the original teaching intent.");
        prompt.append("\nEvery question must include a positive numeric score.");
        prompt.append("\nEvery question must include estimatedTime as a positive integer number of minutes.");
        prompt.append("\nFormula formatting is strict: never output bare TeX or symbolic math outside $...$ or $$...$$.");
        prompt.append("\nIf any field contains only a formula, set notation, matrix, superscript/subscript, or symbolic expression, it still must be wrapped in math delimiters.");
        prompt.append("\nUse \\text{...} for Chinese words inside formulas, and keep that \\text{...} inside the same math delimiters.");
        prompt.append("\nKeep vector, matrix, and identity-matrix bold styles consistent across the whole repaired set. Default to \\boldsymbol{...}; if you choose \\mathbf{...}, use it consistently everywhere in that set.");
        prompt.append("\nWhen formulas contain comma-separated conditions or parallel clauses, use spacing commands such as \\, or \\quad where they genuinely improve readability.");
        prompt.append("\nBefore returning JSON, self-check questionContent, options.optionContent, options.explanation, answers.answerContent, and answers.explanation for naked TeX such as \\frac, \\sqrt, \\mathbb, \\in, \\mid, \\{...\\}, or x^2.");
        prompt.append("\n").append(AIChatConstants.MATH_LATEX_STYLE_PROMPT);
        if (context.getRequest() != null && context.getRequest().getScorePerQuestion() != null) {
            prompt.append("\nUse ")
                    .append(context.getRequest().getScorePerQuestion())
                    .append(" points for each question unless doing so would conflict with the requested total score.");
        }
        if (context.getRequest() != null && context.getRequest().getTotalScore() != null) {
            prompt.append("\nThe sum of all question score values must equal ")
                    .append(context.getRequest().getTotalScore())
                    .append(" points.");
        }
        Integer targetTotalEstimatedTime = resolveTargetTotalEstimatedTime(context);
        if (targetTotalEstimatedTime != null) {
            prompt.append("\nThe sum of all question estimatedTime values must equal ")
                    .append(targetTotalEstimatedTime)
                    .append(" minutes.");
            prompt.append("\nDistribute time reasonably by question type, difficulty and actual solving workload.");
            prompt.append("\nDo not assign the same estimatedTime to every question unless their workload is genuinely identical.");
        }
        prompt.append("\nRepair attempt ").append(attemptNo).append(" of ").append(MAX_REPAIR_ATTEMPTS).append('.');
        prompt.append("\n\nOriginal request JSON:\n").append(JSON.toJSONString(context.getRequest()));
        prompt.append("\n\nBlueprint JSON:\n").append(JSON.toJSONString(context.getBlueprint()));
        prompt.append("\n\nCurrent questions JSON:\n").append(JSON.toJSONString(currentQuestions));
        prompt.append("\n\nValidation issues JSON:\n").append(JSON.toJSONString(issues));
        return prompt.toString();
    }

    private boolean isBetterCandidate(List<QuestionResponseDTO> candidateQuestions,
                                      List<ValidationIssueDTO> candidateIssues,
                                      List<QuestionResponseDTO> bestQuestions,
                                      List<ValidationIssueDTO> bestIssues,
                                      QuestionAgentContext context) {
        int candidateBlocking = blockingIssueCount(candidateIssues);
        int bestBlocking = blockingIssueCount(bestIssues);
        if (candidateBlocking != bestBlocking) {
            return candidateBlocking < bestBlocking;
        }

        int candidateIssueCount = candidateIssues != null ? candidateIssues.size() : 0;
        int bestIssueCount = bestIssues != null ? bestIssues.size() : 0;
        if (candidateIssueCount != bestIssueCount) {
            return candidateIssueCount < bestIssueCount;
        }

        int targetCount = context.getRequest() != null && context.getRequest().getQuestionCount() != null
                ? context.getRequest().getQuestionCount()
                : 0;
        int candidateGap = Math.abs(targetCount - (candidateQuestions != null ? candidateQuestions.size() : 0));
        int bestGap = Math.abs(targetCount - (bestQuestions != null ? bestQuestions.size() : 0));
        if (candidateGap != bestGap) {
            return candidateGap < bestGap;
        }

        return candidateQuestions != null && bestQuestions != null && candidateQuestions.size() > bestQuestions.size();
    }

    private int blockingIssueCount(List<ValidationIssueDTO> issues) {
        if (CollectionUtils.isEmpty(issues)) {
            return 0;
        }

        int count = 0;
        for (ValidationIssueDTO issue : issues) {
            if (issue == null) {
                continue;
            }
            if ("error".equalsIgnoreCase(issue.getLevel())
                    || "DUPLICATE_QUESTION".equalsIgnoreCase(issue.getCode())
                    || "REFERENCE_DUPLICATE".equalsIgnoreCase(issue.getCode())) {
                count++;
            }
        }
        return count;
    }

    private Integer resolveTargetTotalEstimatedTime(QuestionAgentContext context) {
        if (context == null) {
            return null;
        }
        if (context.getRequest() != null && context.getRequest().getTotalEstimatedTime() != null) {
            return context.getRequest().getTotalEstimatedTime();
        }
        return context.getBlueprint() != null ? context.getBlueprint().getTotalEstimatedTime() : null;
    }
}
