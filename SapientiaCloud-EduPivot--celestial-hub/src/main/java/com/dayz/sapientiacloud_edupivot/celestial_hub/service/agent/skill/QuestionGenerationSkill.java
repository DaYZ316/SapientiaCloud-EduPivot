package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGeneratePayload;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRecord;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.assembler.QuestionResponseAssembler;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperSectionPlanDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ExamConstraintTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates draft questions section by section with local retry and duplicate controls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionGenerationSkill implements AgentSkill<List<QuestionDraftDTO>> {

    private static final int MAX_SECTION_ATTEMPTS = 3;
    private static final int ISSUE_SUMMARY_LIMIT = 5;
    private static final int BLOCKED_SIGNATURE_LIMIT = 8;

    private final ChatClient chatClient;
    private final QuestionResponseAssembler questionResponseAssembler;
    private final ExamConstraintTool examConstraintTool;

    @Override
    public String name() {
        return "questionGeneration";
    }

    @Override
    public List<QuestionDraftDTO> execute(QuestionAgentContext context) {
        if (context.getBlueprint() == null || CollectionUtils.isEmpty(context.getBlueprint().getSections())) {
            return List.of();
        }

        List<QuestionDraftDTO> drafts = new ArrayList<>();
        Set<String> blockedSignatures = collectReferenceSignatures(context);
        for (PaperSectionPlanDTO section : context.getBlueprint().getSections()) {
            List<QuestionDraftDTO> sectionDrafts = generateSectionWithRetry(context, section, blockedSignatures);
            drafts.addAll(sectionDrafts);
            blockedSignatures.addAll(extractDraftSignatures(sectionDrafts));
        }
        return drafts;
    }

    private List<QuestionDraftDTO> generateSectionWithRetry(QuestionAgentContext context,
                                                            PaperSectionPlanDTO section,
                                                            Set<String> blockedSignatures) {
        SectionAttempt bestAttempt = null;
        List<String> retryHints = List.of();

        for (int attemptNo = 1; attemptNo <= MAX_SECTION_ATTEMPTS; attemptNo++) {
            SectionAttempt attempt = generateSectionAttempt(context, section, blockedSignatures, retryHints, attemptNo);
            if (isBetterAttempt(attempt, bestAttempt, section)) {
                bestAttempt = attempt;
            }
            if (!hasBlockingIssues(attempt.getIssues())) {
                return toDrafts(section, attempt.getQuestions(), attempt.getIssues(), "generated");
            }
            retryHints = summarizeIssues(attempt.getIssues());
        }

        if (bestAttempt == null || CollectionUtils.isEmpty(bestAttempt.getQuestions())) {
            return List.of();
        }

        log.warn("Section generation exhausted retries. requestId={}, sectionNo={}, issueCount={}",
                context.getRequestId(),
                section.getSectionNo(),
                bestAttempt.getIssues() != null ? bestAttempt.getIssues().size() : 0);
        return toDrafts(section, bestAttempt.getQuestions(), bestAttempt.getIssues(), "retry_exhausted");
    }

    private SectionAttempt generateSectionAttempt(QuestionAgentContext context,
                                                  PaperSectionPlanDTO section,
                                                  Set<String> blockedSignatures,
                                                  List<String> retryHints,
                                                  int attemptNo) {
        try {
            String prompt = buildPrompt(context, section, blockedSignatures, retryHints, attemptNo);
            QuestionGeneratePayload payload = chatClient
                    .prompt(new Prompt(prompt))
                    .call()
                    .entity(QuestionGeneratePayload.class);
            List<QuestionGenerateRecord> records = payload != null ? payload.questionsOrEmpty() : List.of();

            List<QuestionResponseDTO> questions = questionResponseAssembler.fromRecords(
                    records,
                    context.getUserId(),
                    context.getRequestId()
            );
            applyDefaults(questions, context.getRequest(), section);

            List<QuestionResponseDTO> normalizedQuestions = examConstraintTool.normalizeQuestions(
                    questions,
                    buildSectionRequest(section),
                    context.getUserId(),
                    context.getRequestId()
            );
            examConstraintTool.rebalanceQuestionScores(normalizedQuestions, buildSectionRequest(section));
            examConstraintTool.rebalanceEstimatedTimes(normalizedQuestions, section.getTotalEstimatedTime());
            List<ValidationIssueDTO> issues = examConstraintTool.validateSection(
                    normalizedQuestions,
                    section,
                    blockedSignatures
            );

            return new SectionAttempt(normalizedQuestions, issues);
        } catch (Exception e) {
            log.warn("Section generation attempt failed. requestId={}, sectionNo={}, attemptNo={}, error={}",
                    context.getRequestId(), section.getSectionNo(), attemptNo, e.getMessage());

            ValidationIssueDTO issue = new ValidationIssueDTO();
            issue.setCode("SECTION_GENERATION_FAILED");
            issue.setLevel("error");
            issue.setMessage("Model invocation failed during section generation.");
            issue.setRepairHint("Retry the section generation and keep the output strictly as a JSON object with a questions array.");
            return new SectionAttempt(List.of(), List.of(issue));
        }
    }

    private String buildPrompt(QuestionAgentContext context,
                               PaperSectionPlanDTO section,
                               Set<String> blockedSignatures,
                               List<String> retryHints,
                               int attemptNo) {
        StringBuilder prompt = new StringBuilder(QuestionConstants.QUESTION_SYSTEM_PROMPT);
        prompt.append("\n\nCurrent task: generate questions for one blueprint section only.");
        prompt.append("\nReturn a JSON object matching QuestionGeneratePayload and nothing else.");
        prompt.append("\nUse this exact top-level shape: {\"questions\":[...]}.");
        prompt.append("\nThe questions array length must equal ").append(section.getTargetCount()).append('.');
        prompt.append("\nEvery question must include a positive numeric score.");
        prompt.append("\nDo not add markdown fences or explanations.");
        prompt.append("\nEvery question must include estimatedTime as a positive integer number of minutes.");
        prompt.append("\nFormula formatting is strict: never output bare TeX or symbolic math outside $...$ or $$...$$.");
        prompt.append("\nIf any field contains only a formula, set notation, matrix, superscript/subscript, or symbolic expression, it still must be wrapped in math delimiters.");
        prompt.append("\nUse \\text{...} for Chinese words inside formulas, and keep that \\text{...} inside the same math delimiters.");
        prompt.append("\nBefore returning JSON, self-check questionContent, options.optionContent, options.explanation, answers.answerContent, and answers.explanation for naked TeX such as \\frac, \\sqrt, \\mathbb, \\in, \\mid, \\{...\\}, or x^2.");
        prompt.append("\n").append(AIChatConstants.MATH_LATEX_STYLE_PROMPT);
        prompt.append("\nAttempt ").append(attemptNo).append(" of ").append(MAX_SECTION_ATTEMPTS).append('.');
        prompt.append("\n\nGlobal request JSON:\n").append(JSON.toJSONString(context.getRequest()));
        prompt.append("\n\nCurrent section JSON:\n").append(JSON.toJSONString(section));
        if (section.getScorePerQuestion() != null) {
            prompt.append("\nEvery question score in this section must equal ")
                    .append(section.getScorePerQuestion())
                    .append(" points.");
        }
        if (section.getTotalEstimatedTime() != null) {
            prompt.append("\nThe sum of estimatedTime for all questions in this section must equal ")
                    .append(section.getTotalEstimatedTime())
                    .append(" minutes.");
            prompt.append("\nDistribute time reasonably by question type, difficulty and actual solving workload.");
            prompt.append("\nDo not assign the same estimatedTime to every question unless their workload is genuinely identical.");
        }

        if (!CollectionUtils.isEmpty(section.getEvidences())) {
            List<AgentEvidenceDTO> evidences = section.getEvidences();
            int evidenceLimit = Math.min(5, evidences.size());
            prompt.append("\n\nReference evidence JSON:\n")
                    .append(JSON.toJSONString(evidences.subList(0, evidenceLimit)));
        }

        List<String> recentBlockedSignatures = recentBlockedSignatures(blockedSignatures);
        if (!CollectionUtils.isEmpty(recentBlockedSignatures)) {
            prompt.append("\n\nAvoid generating questions that are too similar to these normalized signatures:\n")
                    .append(JSON.toJSONString(recentBlockedSignatures));
        }

        if (attemptNo > 1 && !CollectionUtils.isEmpty(retryHints)) {
            prompt.append("\n\nPrevious attempt issues:\n")
                    .append(JSON.toJSONString(retryHints));
            prompt.append("\nPlease fix every issue in this retry.");
        }

        return prompt.toString();
    }

    private void applyDefaults(List<QuestionResponseDTO> questions,
                               QuestionGenerateRequestDTO request,
                               PaperSectionPlanDTO section) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        for (QuestionResponseDTO question : questions) {
            if (question.getQuestionType() == null) {
                question.setQuestionType(section.getQuestionType());
            }
            if (question.getDifficulty() == null || question.getDifficulty() < 1 || question.getDifficulty() > 3) {
                question.setDifficulty(section.getDifficulty());
            }
            if (question.getScore() == null) {
                if (section.getScorePerQuestion() != null) {
                    question.setScore(section.getScorePerQuestion());
                } else if (request != null && request.getScorePerQuestion() != null) {
                    question.setScore(request.getScorePerQuestion());
                }
            }
            if (question.getEstimatedTime() == null || question.getEstimatedTime() <= 0) {
                question.setEstimatedTime(examConstraintTool.recommendEstimatedTime(
                        question.getQuestionType(),
                        question.getDifficulty()
                ));
            }
            if (CollectionUtils.isEmpty(question.getTags()) && !CollectionUtils.isEmpty(section.getKnowledgePoints())) {
                question.setTags(new ArrayList<>(section.getKnowledgePoints()));
            }
        }
    }

    private List<QuestionDraftDTO> toDrafts(PaperSectionPlanDTO section,
                                            List<QuestionResponseDTO> questions,
                                            List<ValidationIssueDTO> issues,
                                            String reviewStatus) {
        List<QuestionDraftDTO> drafts = new ArrayList<>();
        int draftNo = 1;
        for (QuestionResponseDTO question : questions) {
            QuestionDraftDTO draft = new QuestionDraftDTO();
            draft.setSectionNo(section.getSectionNo());
            draft.setDraftNo(draftNo);
            draft.setQuestion(question);
            if (!CollectionUtils.isEmpty(section.getEvidences())) {
                draft.setEvidences(new ArrayList<>(section.getEvidences()));
            }
            draft.setReviewStatus(reviewStatus);
            draft.setIssues(resolveDraftIssues(issues, draftNo));
            drafts.add(draft);
            draftNo++;
        }
        return drafts;
    }

    private List<ValidationIssueDTO> resolveDraftIssues(List<ValidationIssueDTO> issues, int draftNo) {
        if (CollectionUtils.isEmpty(issues)) {
            return List.of();
        }

        List<ValidationIssueDTO> draftIssues = new ArrayList<>();
        for (ValidationIssueDTO issue : issues) {
            if (issue == null) {
                continue;
            }
            if (issue.getQuestionIndex() == null || issue.getQuestionIndex() == draftNo) {
                draftIssues.add(issue);
            }
        }
        return draftIssues;
    }

    private Set<String> collectReferenceSignatures(QuestionAgentContext context) {
        Set<String> blockedSignatures = new LinkedHashSet<>();
        if (context == null || CollectionUtils.isEmpty(context.getEvidences())) {
            return blockedSignatures;
        }

        for (AgentEvidenceDTO evidence : context.getEvidences()) {
            if (evidence == null || !"question_sample".equalsIgnoreCase(evidence.getSourceType())) {
                continue;
            }
            String signature = examConstraintTool.buildSignature(evidence.getTitle(), evidence.getExcerpt());
            if (StringUtils.hasText(signature)) {
                blockedSignatures.add(signature);
            }
        }
        return blockedSignatures;
    }

    private Set<String> extractDraftSignatures(List<QuestionDraftDTO> drafts) {
        Set<String> signatures = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(drafts)) {
            return signatures;
        }

        for (QuestionDraftDTO draft : drafts) {
            if (draft == null || draft.getQuestion() == null) {
                continue;
            }
            String signature = examConstraintTool.buildSignature(draft.getQuestion());
            if (StringUtils.hasText(signature)) {
                signatures.add(signature);
            }
        }
        return signatures;
    }

    private List<String> summarizeIssues(List<ValidationIssueDTO> issues) {
        List<String> summaries = new ArrayList<>();
        if (CollectionUtils.isEmpty(issues)) {
            return summaries;
        }

        for (ValidationIssueDTO issue : issues) {
            if (issue == null) {
                continue;
            }
            StringBuilder summary = new StringBuilder();
            if (issue.getQuestionIndex() != null) {
                summary.append("question ").append(issue.getQuestionIndex()).append(": ");
            }
            if (StringUtils.hasText(issue.getMessage())) {
                summary.append(issue.getMessage());
            }
            if (StringUtils.hasText(issue.getRepairHint())) {
                if (summary.length() > 0) {
                    summary.append(" Hint: ");
                }
                summary.append(issue.getRepairHint());
            }
            if (summary.length() > 0) {
                summaries.add(summary.toString());
            }
            if (summaries.size() >= ISSUE_SUMMARY_LIMIT) {
                break;
            }
        }
        return summaries;
    }

    private List<String> recentBlockedSignatures(Set<String> blockedSignatures) {
        if (blockedSignatures == null || blockedSignatures.isEmpty()) {
            return List.of();
        }

        List<String> signatureList = new ArrayList<>(blockedSignatures);
        int fromIndex = Math.max(0, signatureList.size() - BLOCKED_SIGNATURE_LIMIT);
        return new ArrayList<>(signatureList.subList(fromIndex, signatureList.size()));
    }

    private boolean hasBlockingIssues(List<ValidationIssueDTO> issues) {
        if (CollectionUtils.isEmpty(issues)) {
            return false;
        }
        for (ValidationIssueDTO issue : issues) {
            if (issue == null) {
                continue;
            }
            if ("error".equalsIgnoreCase(issue.getLevel())
                    || "DUPLICATE_QUESTION".equalsIgnoreCase(issue.getCode())
                    || "REFERENCE_DUPLICATE".equalsIgnoreCase(issue.getCode())) {
                return true;
            }
        }
        return false;
    }

    private boolean isBetterAttempt(SectionAttempt candidate,
                                    SectionAttempt current,
                                    PaperSectionPlanDTO section) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }

        int candidateBlocking = blockingIssueCount(candidate.getIssues());
        int currentBlocking = blockingIssueCount(current.getIssues());
        if (candidateBlocking != currentBlocking) {
            return candidateBlocking < currentBlocking;
        }

        int candidateIssueCount = candidate.getIssues() != null ? candidate.getIssues().size() : 0;
        int currentIssueCount = current.getIssues() != null ? current.getIssues().size() : 0;
        if (candidateIssueCount != currentIssueCount) {
            return candidateIssueCount < currentIssueCount;
        }

        int targetCount = section.getTargetCount() != null ? section.getTargetCount() : 0;
        int candidateGap = Math.abs(targetCount - candidate.questionCount());
        int currentGap = Math.abs(targetCount - current.questionCount());
        if (candidateGap != currentGap) {
            return candidateGap < currentGap;
        }

        return candidate.questionCount() > current.questionCount();
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

    private QuestionGenerateRequestDTO buildSectionRequest(PaperSectionPlanDTO section) {
        QuestionGenerateRequestDTO request = new QuestionGenerateRequestDTO();
        request.setQuestionCount(section.getTargetCount());
        request.setQuestionType(section.getQuestionType());
        request.setDifficulty(section.getDifficulty());
        request.setScorePerQuestion(section.getScorePerQuestion());
        return request;
    }

    private static final class SectionAttempt {

        private final List<QuestionResponseDTO> questions;
        private final List<ValidationIssueDTO> issues;

        private SectionAttempt(List<QuestionResponseDTO> questions, List<ValidationIssueDTO> issues) {
            this.questions = questions != null ? questions : List.of();
            this.issues = issues != null ? issues : List.of();
        }

        private List<QuestionResponseDTO> getQuestions() {
            return questions;
        }

        private List<ValidationIssueDTO> getIssues() {
            return issues;
        }

        private int questionCount() {
            return questions.size();
        }
    }
}
