package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.trace;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperBlueprintDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperSectionPlanDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds compact, UI-friendly trace payloads for SSE progress updates.
 */
public final class QuestionGenerationTracePayloads {

    private static final int DEFAULT_TEXT_LIMIT = 240;
    private static final int DEFAULT_LIST_LIMIT = 6;

    private QuestionGenerationTracePayloads() {
    }

    public static Map<String, Object> evidenceBatch(String query, List<AgentEvidenceDTO> evidences) {
        Map<String, Object> payload = new LinkedHashMap<>();
        putIfHasText(payload, "query", abbreviate(query));
        payload.put("evidenceCount", evidences != null ? evidences.size() : 0);
        payload.put("evidences", summarizeEvidences(evidences, DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static Map<String, Object> blueprint(PaperBlueprintDTO blueprint) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (blueprint == null) {
            payload.put("sectionCount", 0);
            return payload;
        }
        putIfHasText(payload, "blueprintId", blueprint.getBlueprintId());
        putIfHasText(payload, "generationStrategy", blueprint.getGenerationStrategy());
        payload.put("totalQuestionCount", blueprint.getTotalQuestionCount());
        payload.put("totalScore", blueprint.getTotalScore());
        payload.put("totalEstimatedTime", blueprint.getTotalEstimatedTime());
        payload.put("sectionCount", blueprint.getSections() != null ? blueprint.getSections().size() : 0);
        payload.put("sections", summarizeSections(blueprint.getSections(), DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static Map<String, Object> sectionAttempt(PaperSectionPlanDTO section,
                                                     int attemptNo,
                                                     List<QuestionResponseDTO> questions,
                                                     List<ValidationIssueDTO> issues) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (section != null) {
            payload.put("sectionNo", section.getSectionNo());
            putIfHasText(payload, "sectionTitle", section.getSectionTitle());
            payload.put("questionType", section.getQuestionType());
            payload.put("difficulty", section.getDifficulty());
            payload.put("targetCount", section.getTargetCount());
            payload.put("sectionTotalEstimatedTime", section.getTotalEstimatedTime());
        }
        payload.put("attemptNo", attemptNo);
        payload.put("questionCount", questions != null ? questions.size() : 0);
        payload.put("issueCount", issues != null ? issues.size() : 0);
        payload.put("questions", summarizeQuestions(questions, DEFAULT_LIST_LIMIT));
        payload.put("issues", summarizeIssues(issues, DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static Map<String, Object> validation(List<QuestionResponseDTO> questions,
                                                 List<ValidationIssueDTO> issues) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("questionCount", questions != null ? questions.size() : 0);
        payload.put("issueCount", issues != null ? issues.size() : 0);
        payload.put("questions", summarizeQuestions(questions, DEFAULT_LIST_LIMIT));
        payload.put("issues", summarizeIssues(issues, DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static Map<String, Object> repairAttempt(int attemptNo,
                                                    List<QuestionResponseDTO> questions,
                                                    List<ValidationIssueDTO> issues) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("attemptNo", attemptNo);
        payload.put("questionCount", questions != null ? questions.size() : 0);
        payload.put("issueCount", issues != null ? issues.size() : 0);
        payload.put("questions", summarizeQuestions(questions, DEFAULT_LIST_LIMIT));
        payload.put("issues", summarizeIssues(issues, DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static Map<String, Object> finalQuestions(List<QuestionResponseDTO> questions,
                                                     List<ValidationIssueDTO> issues) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("questionCount", questions != null ? questions.size() : 0);
        payload.put("remainingIssueCount", issues != null ? issues.size() : 0);
        payload.put("questions", summarizeQuestions(questions, DEFAULT_LIST_LIMIT));
        payload.put("issues", summarizeIssues(issues, DEFAULT_LIST_LIMIT));
        return payload;
    }

    public static List<Map<String, Object>> summarizeEvidences(List<AgentEvidenceDTO> evidences, int limit) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(evidences)) {
            return items;
        }
        int safeLimit = Math.max(1, limit);
        for (AgentEvidenceDTO evidence : evidences) {
            if (evidence == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            putIfHasText(item, "sourceType", evidence.getSourceType());
            putIfHasText(item, "sourceId", evidence.getSourceId());
            putIfHasText(item, "title", evidence.getTitle());
            putIfHasText(item, "excerpt", abbreviate(evidence.getExcerpt()));
            if (evidence.getScore() != null) {
                item.put("score", evidence.getScore());
            }
            if (evidence.getMetadata() != null && !evidence.getMetadata().isEmpty()) {
                item.put("metadata", new LinkedHashMap<>(evidence.getMetadata()));
            }
            items.add(item);
            if (items.size() >= safeLimit) {
                break;
            }
        }
        return items;
    }

    public static List<Map<String, Object>> summarizeSections(List<PaperSectionPlanDTO> sections, int limit) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(sections)) {
            return items;
        }
        int safeLimit = Math.max(1, limit);
        for (PaperSectionPlanDTO section : sections) {
            if (section == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sectionNo", section.getSectionNo());
            putIfHasText(item, "sectionTitle", section.getSectionTitle());
            item.put("questionType", section.getQuestionType());
            item.put("difficulty", section.getDifficulty());
            item.put("targetCount", section.getTargetCount());
            item.put("scorePerQuestion", section.getScorePerQuestion());
            item.put("estimatedTimePerQuestion", section.getEstimatedTimePerQuestion());
            item.put("totalEstimatedTime", section.getTotalEstimatedTime());
            if (!CollectionUtils.isEmpty(section.getKnowledgePoints())) {
                item.put("knowledgePoints", new ArrayList<>(section.getKnowledgePoints()));
            }
            if (!CollectionUtils.isEmpty(section.getEvidences())) {
                item.put("evidences", summarizeEvidences(section.getEvidences(), 3));
            }
            items.add(item);
            if (items.size() >= safeLimit) {
                break;
            }
        }
        return items;
    }

    public static List<Map<String, Object>> summarizeQuestions(List<QuestionResponseDTO> questions, int limit) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(questions)) {
            return items;
        }
        int safeLimit = Math.max(1, limit);
        for (QuestionResponseDTO question : questions) {
            if (question == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            putIfHasText(item, "id", question.getId() != null ? question.getId().toString() : null);
            putIfHasText(item, "questionTitle", abbreviate(question.getQuestionTitle()));
            putIfHasText(item, "questionContent", abbreviate(question.getQuestionContent()));
            item.put("questionType", question.getQuestionType());
            item.put("difficulty", question.getDifficulty());
            item.put("score", question.getScore());
            item.put("estimatedTime", question.getEstimatedTime());
            if (!CollectionUtils.isEmpty(question.getTags())) {
                item.put("tags", new ArrayList<>(question.getTags()));
            }
            items.add(item);
            if (items.size() >= safeLimit) {
                break;
            }
        }
        return items;
    }

    public static List<Map<String, Object>> summarizeIssues(List<ValidationIssueDTO> issues, int limit) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(issues)) {
            return items;
        }
        int safeLimit = Math.max(1, limit);
        for (ValidationIssueDTO issue : issues) {
            if (issue == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            putIfHasText(item, "code", issue.getCode());
            putIfHasText(item, "level", issue.getLevel());
            putIfHasText(item, "message", abbreviate(issue.getMessage()));
            item.put("questionIndex", issue.getQuestionIndex());
            putIfHasText(item, "repairHint", abbreviate(issue.getRepairHint()));
            items.add(item);
            if (items.size() >= safeLimit) {
                break;
            }
        }
        return items;
    }

    public static String abbreviate(String value) {
        return abbreviate(value, DEFAULT_TEXT_LIMIT);
    }

    public static String abbreviate(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= Math.max(1, maxLength)) {
            return normalized;
        }
        return normalized.substring(0, Math.max(1, maxLength - 3)) + "...";
    }

    private static void putIfHasText(Map<String, Object> target, String key, String value) {
        if (target == null || !StringUtils.hasText(key) || !StringUtils.hasText(value)) {
            return;
        }
        target.put(key, value);
    }
}
