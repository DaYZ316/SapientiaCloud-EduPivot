package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionAnswerSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionOptionSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperSectionPlanDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Applies deterministic validation and normalization before or after model calls.
 */
@Component
public class ExamConstraintTool implements AgentTool {

    private static final int TYPE_SINGLE = 0;
    private static final int TYPE_MULTI = 1;
    private static final int TYPE_JUDGE = 2;
    private static final int TYPE_BLANK = 3;
    private static final int TYPE_SHORT = 4;

    @Override
    public String name() {
        return "examConstraint";
    }

    public List<QuestionResponseDTO> normalizeQuestions(List<QuestionResponseDTO> questions,
                                                        QuestionGenerateRequestDTO request,
                                                        UUID userId,
                                                        String requestId) {
        List<QuestionResponseDTO> normalized = new ArrayList<>();
        if (questions == null) {
            return normalized;
        }

        int answerOrderFallback = 1;
        for (QuestionResponseDTO question : questions) {
            if (question == null) {
                continue;
            }

            if (question.getId() == null) {
                question.setId(UuidCreator.getTimeOrderedEpoch());
            }
            if (question.getSysUserId() == null) {
                question.setSysUserId(userId);
            }
            if (!StringUtils.hasText(question.getRequestId())) {
                question.setRequestId(requestId);
            }
            if (question.getQuestionType() == null || question.getQuestionType() < 0 || question.getQuestionType() > 4) {
                if (request != null && request.getQuestionType() != null && request.getQuestionType() >= 0 && request.getQuestionType() <= 4) {
                    question.setQuestionType(request.getQuestionType());
                }
            }
            if (question.getDifficulty() == null || question.getDifficulty() < 1 || question.getDifficulty() > 3) {
                if (request != null && request.getDifficulty() != null && request.getDifficulty() >= 1 && request.getDifficulty() <= 3) {
                    question.setDifficulty(request.getDifficulty());
                } else {
                    question.setDifficulty(2);
                }
            }
            if (question.getScore() == null && request != null && request.getScorePerQuestion() != null) {
                question.setScore(request.getScorePerQuestion());
            }
            if (question.getEstimatedTime() == null || question.getEstimatedTime() <= 0) {
                question.setEstimatedTime(recommendEstimatedTime(question.getQuestionType(), question.getDifficulty()));
            }

            normalizeChoiceOptions(question);
            normalizeAnswers(question, answerOrderFallback);
            answerOrderFallback++;
            normalized.add(question);
        }

        return normalized;
    }

    public void rebalanceQuestionScores(List<QuestionResponseDTO> questions, QuestionGenerateRequestDTO request) {
        if (CollectionUtils.isEmpty(questions) || request == null) {
            return;
        }

        BigDecimal requestedScorePerQuestion = normalizePositiveScore(request.getScorePerQuestion());
        BigDecimal requestedTotalScore = normalizePositiveScore(request.getTotalScore());

        if (requestedScorePerQuestion != null && requestedTotalScore == null) {
            applyUniformQuestionScores(questions, requestedScorePerQuestion);
            return;
        }

        if (requestedTotalScore == null) {
            synchronizeNestedScores(questions);
            return;
        }

        if (requestedScorePerQuestion != null) {
            BigDecimal expectedUniformTotal = requestedScorePerQuestion.multiply(BigDecimal.valueOf(questions.size()));
            if (expectedUniformTotal.compareTo(requestedTotalScore) == 0) {
                applyUniformQuestionScores(questions, requestedScorePerQuestion);
                return;
            }
        }

        List<Long> weights = resolveQuestionScoreWeights(questions);
        List<BigDecimal> balancedScores = allocateWeightedScores(weights, requestedTotalScore);
        for (int i = 0; i < questions.size(); i++) {
            QuestionResponseDTO question = questions.get(i);
            if (question == null) {
                continue;
            }
            question.setScore(balancedScores.get(i));
            synchronizeNestedScore(question);
        }
    }

    public void rebalanceEstimatedTimes(List<QuestionResponseDTO> questions, Integer targetTotalTime) {
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }

        List<Integer> weights = new ArrayList<>(questions.size());
        for (QuestionResponseDTO question : questions) {
            int suggested = resolveSuggestedEstimatedTime(question);
            if (question.getEstimatedTime() == null || question.getEstimatedTime() <= 0) {
                question.setEstimatedTime(suggested);
            }
            weights.add(Math.max(1, question.getEstimatedTime()));
        }

        if (targetTotalTime == null || targetTotalTime <= 0) {
            return;
        }

        int minimumTotal = questions.size();
        int effectiveTarget = Math.max(targetTotalTime, minimumTotal);
        List<Integer> balancedTimes = allocateWeightedIntegers(weights, effectiveTarget, 1);
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setEstimatedTime(balancedTimes.get(i));
        }
    }

    public int recommendEstimatedTime(Integer questionType, Integer difficulty) {
        int normalizedDifficulty = normalizeDifficulty(difficulty);
        return switch (questionType != null ? questionType : -1) {
            case TYPE_JUDGE -> switch (normalizedDifficulty) {
                case 1 -> 1;
                case 2 -> 2;
                default -> 3;
            };
            case TYPE_SINGLE -> switch (normalizedDifficulty) {
                case 1 -> 2;
                case 2 -> 3;
                default -> 4;
            };
            case TYPE_MULTI -> switch (normalizedDifficulty) {
                case 1 -> 3;
                case 2 -> 4;
                default -> 6;
            };
            case TYPE_BLANK -> switch (normalizedDifficulty) {
                case 1 -> 3;
                case 2 -> 4;
                default -> 6;
            };
            case TYPE_SHORT -> switch (normalizedDifficulty) {
                case 1 -> 5;
                case 2 -> 7;
                default -> 10;
            };
            default -> switch (normalizedDifficulty) {
                case 1 -> 2;
                case 2 -> 3;
                default -> 5;
            };
        };
    }

    public List<ValidationIssueDTO> validate(List<QuestionResponseDTO> questions, QuestionGenerateRequestDTO request) {
        return validate(questions, request, Set.of());
    }

    public List<ValidationIssueDTO> validate(List<QuestionResponseDTO> questions,
                                             QuestionGenerateRequestDTO request,
                                             Set<String> blockedSignatures) {
        Integer expectedCount = request != null ? request.getQuestionCount() : null;
        BigDecimal expectedTotalScore = request != null ? request.getTotalScore() : null;
        Integer expectedTotalEstimatedTime = request != null ? request.getTotalEstimatedTime() : null;
        return validateInternal(questions, expectedCount, expectedTotalScore, expectedTotalEstimatedTime, blockedSignatures);
    }

    public List<ValidationIssueDTO> validateSection(List<QuestionResponseDTO> questions,
                                                    PaperSectionPlanDTO section,
                                                    Set<String> blockedSignatures) {
        Integer expectedCount = section != null ? section.getTargetCount() : null;
        Integer expectedTotalEstimatedTime = section != null ? section.getTotalEstimatedTime() : null;
        return validateInternal(questions, expectedCount, null, expectedTotalEstimatedTime, blockedSignatures);
    }

    public String buildSignature(QuestionResponseDTO question) {
        if (question == null) {
            return "";
        }
        return buildSignature(question.getQuestionTitle(), question.getQuestionContent());
    }

    public String buildSignature(String questionTitle, String questionContent) {
        String normalizedTitle = normalizeText(questionTitle);
        String normalizedContent = normalizeText(questionContent);
        String signature = (normalizedTitle + '|' + normalizedContent)
                .replaceAll("^\\|+", "")
                .replaceAll("\\|+$", "")
                .trim();
        return signature;
    }

    private List<ValidationIssueDTO> validateInternal(List<QuestionResponseDTO> questions,
                                                      Integer expectedCount,
                                                      BigDecimal expectedTotalScore,
                                                      Integer expectedTotalEstimatedTime,
                                                      Set<String> blockedSignatures) {
        List<ValidationIssueDTO> issues = new ArrayList<>();
        if (questions == null || questions.isEmpty()) {
            issues.add(issue("EMPTY_RESULT", "error", "No questions were generated.", null,
                    "Generate at least one valid question."));
            return issues;
        }

        if (expectedCount != null && expectedCount != questions.size()) {
            issues.add(issue("QUESTION_COUNT_MISMATCH", "error",
                    "Generated question count does not match the expected count.",
                    null,
                    "Return exactly " + expectedCount + " questions."));
        }

        if (expectedTotalScore != null) {
            BigDecimal totalScore = BigDecimal.ZERO;
            for (QuestionResponseDTO question : questions) {
                if (question != null && question.getScore() != null) {
                    totalScore = totalScore.add(question.getScore());
                }
            }
            if (totalScore.compareTo(expectedTotalScore) != 0) {
                issues.add(issue("TOTAL_SCORE_MISMATCH", "warn",
                        "Generated total score does not match requested total score.",
                        null,
                        "Adjust question scores so the total score equals " + expectedTotalScore + '.'));
            }
        }

        if (expectedTotalEstimatedTime != null) {
            int totalEstimatedTime = 0;
            for (QuestionResponseDTO question : questions) {
                if (question != null && question.getEstimatedTime() != null) {
                    totalEstimatedTime += question.getEstimatedTime();
                }
            }
            if (totalEstimatedTime != expectedTotalEstimatedTime) {
                issues.add(issue("TOTAL_ESTIMATED_TIME_MISMATCH", "warn",
                        "Generated total estimated time does not match requested total estimated time.",
                        null,
                        "Adjust question estimatedTime so the total time equals " + expectedTotalEstimatedTime + '.'));
            }
        }

        Set<String> normalizedBlockedSignatures = blockedSignatures != null ? blockedSignatures : Set.of();
        Map<String, Integer> duplicateMap = new HashMap<>();
        for (int i = 0; i < questions.size(); i++) {
            QuestionResponseDTO question = questions.get(i);
            int index = i + 1;
            if (question == null) {
                issues.add(issue("NULL_QUESTION", "error", "Question is null.", index,
                        "Return a non-null question object."));
                continue;
            }

            if (!StringUtils.hasText(question.getQuestionTitle()) && !StringUtils.hasText(question.getQuestionContent())) {
                issues.add(issue("EMPTY_CONTENT", "error", "Question title and content are both empty.", index,
                        "Provide at least a title or content."));
            }
            if (question.getQuestionType() == null || question.getQuestionType() < 0 || question.getQuestionType() > 4) {
                issues.add(issue("INVALID_TYPE", "error", "Question type is invalid.", index,
                        "Use a value between 0 and 4."));
                continue;
            }
            if (question.getDifficulty() == null || question.getDifficulty() < 1 || question.getDifficulty() > 3) {
                issues.add(issue("INVALID_DIFFICULTY", "error", "Question difficulty is invalid.", index,
                        "Use a difficulty between 1 and 3."));
            }
            if (question.getScore() == null || question.getScore().compareTo(BigDecimal.ZERO) <= 0) {
                issues.add(issue("INVALID_SCORE", "error", "Question score is missing or non-positive.", index,
                        "Provide a positive question score."));
            }
            if (question.getEstimatedTime() == null || question.getEstimatedTime() <= 0) {
                issues.add(issue("INVALID_TIME", "warn", "Estimated time is missing or invalid.", index,
                        "Provide a positive estimated time."));
            }

            String signature = buildSignature(question);
            if (StringUtils.hasText(signature)) {
                if (normalizedBlockedSignatures.contains(signature)) {
                    issues.add(issue("REFERENCE_DUPLICATE", "warn",
                            "Question content is too similar to existing question bank samples or accepted drafts.",
                            index,
                            "Rewrite the question stem so it stays aligned with the requirement but is not repeated."));
                }
                Integer firstIndex = duplicateMap.putIfAbsent(signature, index);
                if (firstIndex != null) {
                    issues.add(issue("DUPLICATE_QUESTION", "warn",
                            "Question content appears duplicated with question " + firstIndex + '.',
                            index,
                            "Rewrite the question to avoid duplication."));
                }
            }

            validateStructure(question, index, issues);
        }

        return issues;
    }

    private void normalizeChoiceOptions(QuestionResponseDTO question) {
        if (question.getQuestionType() == null) {
            return;
        }

        if (question.getQuestionType() == TYPE_SINGLE
                || question.getQuestionType() == TYPE_MULTI
                || question.getQuestionType() == TYPE_JUDGE) {
            question.setAnswers(null);
            if (question.getOptions() == null) {
                return;
            }
            List<QuestionOptionSimpleDTO> normalizedOptions = new ArrayList<>();
            char label = 'A';
            for (QuestionOptionSimpleDTO option : question.getOptions()) {
                if (option == null || !StringUtils.hasText(option.getOptionContent())) {
                    continue;
                }
                if (option.getId() == null) {
                    option.setId(UuidCreator.getTimeOrderedEpoch());
                }
                option.setQuestionId(question.getId());
                if (!StringUtils.hasText(option.getOptionLabel())) {
                    option.setOptionLabel(String.valueOf(label));
                }
                label++;
                normalizedOptions.add(option);
            }
            question.setOptions(normalizedOptions.isEmpty() ? null : normalizedOptions);
            return;
        }

        question.setOptions(null);
    }

    private void normalizeAnswers(QuestionResponseDTO question, int answerOrderFallback) {
        if (question.getQuestionType() == null) {
            return;
        }
        if (question.getQuestionType() == TYPE_BLANK || question.getQuestionType() == TYPE_SHORT) {
            if (question.getAnswers() == null) {
                return;
            }
            List<QuestionAnswerSimpleDTO> normalizedAnswers = new ArrayList<>();
            int sortOrder = 1;
            for (QuestionAnswerSimpleDTO answer : question.getAnswers()) {
                if (answer == null || !StringUtils.hasText(answer.getAnswerContent())) {
                    continue;
                }
                if (answer.getId() == null) {
                    answer.setId(UuidCreator.getTimeOrderedEpoch());
                }
                answer.setQuestionId(question.getId());
                if (answer.getSortOrder() == null || answer.getSortOrder() <= 0) {
                    answer.setSortOrder(sortOrder);
                }
                sortOrder = answer.getSortOrder() + 1;
                normalizedAnswers.add(answer);
            }
            if (CollectionUtils.isEmpty(normalizedAnswers) && question.getQuestionType() == TYPE_SHORT
                    && StringUtils.hasText(question.getQuestionContent())) {
                QuestionAnswerSimpleDTO answer = new QuestionAnswerSimpleDTO();
                answer.setId(UuidCreator.getTimeOrderedEpoch());
                answer.setQuestionId(question.getId());
                answer.setAnswerContent("See explanation.");
                answer.setSortOrder(answerOrderFallback);
                normalizedAnswers.add(answer);
            }
            question.setAnswers(normalizedAnswers.isEmpty() ? null : normalizedAnswers);
            return;
        }

        question.setAnswers(null);
    }

    private void validateStructure(QuestionResponseDTO question, int index, List<ValidationIssueDTO> issues) {
        int questionType = question.getQuestionType();
        if (questionType == TYPE_SINGLE || questionType == TYPE_MULTI || questionType == TYPE_JUDGE) {
            List<QuestionOptionSimpleDTO> options = question.getOptions();
            if (CollectionUtils.isEmpty(options)) {
                issues.add(issue("MISSING_OPTIONS", "error", "Choice-style question is missing options.", index,
                        "Provide valid options and keep answers empty."));
                return;
            }

            int correctCount = 0;
            for (QuestionOptionSimpleDTO option : options) {
                if (option != null && Integer.valueOf(1).equals(option.getIsCorrect())) {
                    correctCount++;
                }
            }
            if ((questionType == TYPE_SINGLE || questionType == TYPE_JUDGE) && correctCount != 1) {
                issues.add(issue("INVALID_CORRECT_COUNT", "error",
                        "Single-choice or judge question must have exactly one correct option.",
                        index,
                        "Mark exactly one option as correct."));
            }
            if (questionType == TYPE_MULTI && correctCount < 2) {
                issues.add(issue("INVALID_MULTI_CORRECT_COUNT", "error",
                        "Multiple-choice question must have at least two correct options.",
                        index,
                        "Mark at least two options as correct."));
            }
            if (questionType == TYPE_JUDGE && options.size() != 2) {
                issues.add(issue("INVALID_JUDGE_OPTION_COUNT", "error",
                        "Judge question must have exactly two options.",
                        index,
                        "Keep only two options for judge questions."));
            }
            if (!CollectionUtils.isEmpty(question.getAnswers())) {
                issues.add(issue("UNEXPECTED_ANSWERS", "warn",
                        "Choice-style question should not contain answers list.",
                        index,
                        "Remove the answers list."));
            }
            return;
        }

        if (questionType == TYPE_BLANK || questionType == TYPE_SHORT) {
            if (CollectionUtils.isEmpty(question.getAnswers())) {
                issues.add(issue("MISSING_ANSWERS", "error", "Open-ended question is missing answers.", index,
                        "Provide valid answers and keep options empty."));
                return;
            }
            Set<Integer> sortOrders = new HashSet<>();
            for (QuestionAnswerSimpleDTO answer : question.getAnswers()) {
                if (answer == null || !StringUtils.hasText(answer.getAnswerContent())) {
                    issues.add(issue("EMPTY_ANSWER", "error", "Answer content is empty.", index,
                            "Fill each answer with non-empty content."));
                    continue;
                }
                if (questionType == TYPE_BLANK) {
                    if (answer.getSortOrder() == null || answer.getSortOrder() <= 0) {
                        issues.add(issue("INVALID_SORT_ORDER", "error",
                                "Blank question answer sort order is invalid.",
                                index,
                                "Use positive and continuous sort orders."));
                    } else if (!sortOrders.add(answer.getSortOrder())) {
                        issues.add(issue("DUPLICATE_SORT_ORDER", "error",
                                "Blank question answer sort order is duplicated.",
                                index,
                                "Keep unique sort orders for each blank."));
                    }
                }
            }
            if (!CollectionUtils.isEmpty(question.getOptions())) {
                issues.add(issue("UNEXPECTED_OPTIONS", "warn",
                        "Open-ended question should not contain options list.",
                        index,
                        "Remove the options list."));
            }
        }
    }

    private ValidationIssueDTO issue(String code, String level, String message, Integer questionIndex, String repairHint) {
        ValidationIssueDTO issue = new ValidationIssueDTO();
        issue.setCode(code);
        issue.setLevel(level);
        issue.setMessage(message);
        issue.setQuestionIndex(questionIndex);
        issue.setRepairHint(repairHint);
        return issue;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value
                .toLowerCase(Locale.ROOT)
                .replace('\u3000', ' ')
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int resolveSuggestedEstimatedTime(QuestionResponseDTO question) {
        if (question == null) {
            return 3;
        }
        return recommendEstimatedTime(question.getQuestionType(), question.getDifficulty());
    }

    private void applyUniformQuestionScores(List<QuestionResponseDTO> questions, BigDecimal scorePerQuestion) {
        if (CollectionUtils.isEmpty(questions) || scorePerQuestion == null) {
            return;
        }
        BigDecimal normalizedScore = normalizeAllocatedScore(scorePerQuestion);
        for (QuestionResponseDTO question : questions) {
            if (question == null) {
                continue;
            }
            question.setScore(normalizedScore);
            synchronizeNestedScore(question);
        }
    }

    private void synchronizeNestedScores(List<QuestionResponseDTO> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        for (QuestionResponseDTO question : questions) {
            synchronizeNestedScore(question);
        }
    }

    private void synchronizeNestedScore(QuestionResponseDTO question) {
        if (question == null || normalizePositiveScore(question.getScore()) == null) {
            return;
        }

        if (question.getQuestionType() != null
                && (question.getQuestionType() == TYPE_SINGLE
                || question.getQuestionType() == TYPE_MULTI
                || question.getQuestionType() == TYPE_JUDGE)) {
            synchronizeOptionScores(question);
            return;
        }

        if (question.getQuestionType() != null
                && (question.getQuestionType() == TYPE_BLANK || question.getQuestionType() == TYPE_SHORT)) {
            synchronizeAnswerScores(question);
        }
    }

    private void synchronizeOptionScores(QuestionResponseDTO question) {
        if (CollectionUtils.isEmpty(question.getOptions())) {
            return;
        }

        List<QuestionOptionSimpleDTO> correctOptions = new ArrayList<>();
        for (QuestionOptionSimpleDTO option : question.getOptions()) {
            if (option != null && Integer.valueOf(1).equals(option.getIsCorrect())) {
                correctOptions.add(option);
            }
        }

        if (correctOptions.isEmpty()) {
            return;
        }

        if (question.getQuestionType() == TYPE_SINGLE
                || question.getQuestionType() == TYPE_JUDGE
                || correctOptions.size() == 1) {
            for (QuestionOptionSimpleDTO option : question.getOptions()) {
                if (option == null) {
                    continue;
                }
                option.setScore(Integer.valueOf(1).equals(option.getIsCorrect())
                        ? normalizeAllocatedScore(question.getScore())
                        : BigDecimal.ZERO);
            }
            return;
        }

        List<Long> weights = new ArrayList<>(correctOptions.size());
        for (QuestionOptionSimpleDTO option : correctOptions) {
            weights.add(resolveNestedScoreWeight(option != null ? option.getScore() : null));
        }
        List<BigDecimal> distributedScores = allocateWeightedScores(weights, question.getScore());
        int distributedIndex = 0;
        for (QuestionOptionSimpleDTO option : question.getOptions()) {
            if (option == null) {
                continue;
            }
            if (Integer.valueOf(1).equals(option.getIsCorrect())) {
                option.setScore(distributedScores.get(distributedIndex++));
            } else {
                option.setScore(BigDecimal.ZERO);
            }
        }
    }

    private void synchronizeAnswerScores(QuestionResponseDTO question) {
        if (CollectionUtils.isEmpty(question.getAnswers())) {
            return;
        }

        List<QuestionAnswerSimpleDTO> answers = new ArrayList<>();
        for (QuestionAnswerSimpleDTO answer : question.getAnswers()) {
            if (answer != null) {
                answers.add(answer);
            }
        }
        if (answers.isEmpty()) {
            return;
        }

        List<Long> weights = new ArrayList<>(answers.size());
        for (QuestionAnswerSimpleDTO answer : answers) {
            weights.add(resolveNestedScoreWeight(answer.getScore()));
        }
        List<BigDecimal> distributedScores = allocateWeightedScores(weights, question.getScore());
        for (int i = 0; i < answers.size(); i++) {
            answers.get(i).setScore(distributedScores.get(i));
        }
    }

    private int normalizeDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty < 1 || difficulty > 3) {
            return 2;
        }
        return difficulty;
    }

    private List<Integer> allocateWeightedIntegers(List<Integer> weights, int targetTotal, int minimumPerItem) {
        int itemCount = weights != null ? weights.size() : 0;
        if (itemCount == 0) {
            return List.of();
        }

        int normalizedMinimum = Math.max(0, minimumPerItem);
        int minimumTotal = itemCount * normalizedMinimum;
        int effectiveTarget = Math.max(targetTotal, minimumTotal);
        int remaining = effectiveTarget - minimumTotal;

        List<Integer> allocations = new ArrayList<>(itemCount);
        List<Double> remainders = new ArrayList<>(itemCount);
        long totalWeight = 0L;
        for (Integer weight : weights) {
            totalWeight += Math.max(1, weight != null ? weight : 1);
            allocations.add(normalizedMinimum);
            remainders.add(0D);
        }

        if (remaining == 0 || totalWeight <= 0L) {
            return allocations;
        }

        int allocatedExtra = 0;
        for (int i = 0; i < itemCount; i++) {
            int weight = Math.max(1, weights.get(i) != null ? weights.get(i) : 1);
            double rawExtra = (double) remaining * weight / totalWeight;
            int extra = (int) Math.floor(rawExtra);
            allocations.set(i, allocations.get(i) + extra);
            remainders.set(i, rawExtra - extra);
            allocatedExtra += extra;
        }

        int leftover = remaining - allocatedExtra;
        while (leftover > 0) {
            int targetIndex = 0;
            double maxRemainder = -1D;
            for (int i = 0; i < itemCount; i++) {
                double remainder = remainders.get(i);
                if (remainder > maxRemainder) {
                    maxRemainder = remainder;
                    targetIndex = i;
                }
            }
            allocations.set(targetIndex, allocations.get(targetIndex) + 1);
            remainders.set(targetIndex, 0D);
            leftover--;
        }

        return allocations;
    }

    private List<Long> resolveQuestionScoreWeights(List<QuestionResponseDTO> questions) {
        List<Long> weights = new ArrayList<>(questions != null ? questions.size() : 0);
        if (CollectionUtils.isEmpty(questions)) {
            return weights;
        }

        for (QuestionResponseDTO question : questions) {
            BigDecimal score = question != null ? normalizePositiveScore(question.getScore()) : null;
            if (score != null) {
                weights.add(toWeight(score));
            } else {
                weights.add((long) Math.max(1, resolveSuggestedEstimatedTime(question)));
            }
        }
        return weights;
    }

    private long resolveNestedScoreWeight(BigDecimal score) {
        BigDecimal positiveScore = normalizePositiveScore(score);
        return positiveScore != null ? toWeight(positiveScore) : 1L;
    }

    private long toWeight(BigDecimal score) {
        if (score == null) {
            return 1L;
        }
        return Math.max(1L, score.movePointRight(2).setScale(0, RoundingMode.CEILING).longValue());
    }

    private List<BigDecimal> allocateWeightedScores(List<Long> weights, BigDecimal targetTotal) {
        int itemCount = weights != null ? weights.size() : 0;
        if (itemCount == 0 || targetTotal == null) {
            return List.of();
        }

        int scale = resolveScoreAllocationScale(targetTotal, itemCount);
        long targetUnits = toScaledUnits(targetTotal, scale);
        List<Long> allocations = allocateWeightedUnits(weights, targetUnits, 1L);

        List<BigDecimal> results = new ArrayList<>(allocations.size());
        for (Long allocation : allocations) {
            results.add(normalizeAllocatedScore(BigDecimal.valueOf(allocation).movePointLeft(scale)));
        }
        return results;
    }

    private int resolveScoreAllocationScale(BigDecimal targetTotal, int itemCount) {
        BigDecimal normalizedTarget = normalizePositiveScore(targetTotal);
        if (normalizedTarget == null || itemCount <= 0) {
            return 0;
        }

        int scale = Math.max(0, normalizedTarget.stripTrailingZeros().scale());
        if (scale == 0 && normalizedTarget.compareTo(BigDecimal.valueOf(itemCount)) >= 0) {
            return 0;
        }

        scale = Math.max(1, scale);
        while (normalizedTarget.movePointRight(scale).compareTo(BigDecimal.valueOf(itemCount)) < 0) {
            scale++;
        }
        return scale;
    }

    private long toScaledUnits(BigDecimal score, int scale) {
        return normalizePositiveScore(score)
                .movePointRight(scale)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
    }

    private List<Long> allocateWeightedUnits(List<Long> weights, long targetUnits, long minimumPerItem) {
        int itemCount = weights != null ? weights.size() : 0;
        if (itemCount == 0) {
            return List.of();
        }

        long normalizedMinimum = Math.max(1L, minimumPerItem);
        long minimumTotal = itemCount * normalizedMinimum;
        long effectiveTarget = Math.max(targetUnits, minimumTotal);
        long remaining = effectiveTarget - minimumTotal;

        List<Long> allocations = new ArrayList<>(itemCount);
        List<Double> remainders = new ArrayList<>(itemCount);
        long totalWeight = 0L;
        for (Long weight : weights) {
            totalWeight += Math.max(1L, weight != null ? weight : 1L);
            allocations.add(normalizedMinimum);
            remainders.add(0D);
        }

        if (remaining == 0L || totalWeight <= 0L) {
            return allocations;
        }

        long allocatedExtra = 0L;
        for (int i = 0; i < itemCount; i++) {
            long weight = Math.max(1L, weights.get(i) != null ? weights.get(i) : 1L);
            double rawExtra = (double) remaining * weight / totalWeight;
            long extra = (long) Math.floor(rawExtra);
            allocations.set(i, allocations.get(i) + extra);
            remainders.set(i, rawExtra - extra);
            allocatedExtra += extra;
        }

        long leftover = remaining - allocatedExtra;
        while (leftover > 0L) {
            int targetIndex = 0;
            double maxRemainder = -1D;
            for (int i = 0; i < itemCount; i++) {
                double remainder = remainders.get(i);
                if (remainder > maxRemainder) {
                    maxRemainder = remainder;
                    targetIndex = i;
                }
            }
            allocations.set(targetIndex, allocations.get(targetIndex) + 1L);
            remainders.set(targetIndex, 0D);
            leftover--;
        }

        return allocations;
    }

    private BigDecimal normalizePositiveScore(BigDecimal score) {
        if (score == null || score.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal normalized = score.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0, RoundingMode.UNNECESSARY) : normalized;
    }

    private BigDecimal normalizeAllocatedScore(BigDecimal score) {
        if (score == null) {
            return null;
        }
        BigDecimal normalized = score.stripTrailingZeros();
        return normalized.scale() < 0 ? normalized.setScale(0, RoundingMode.UNNECESSARY) : normalized;
    }
}
