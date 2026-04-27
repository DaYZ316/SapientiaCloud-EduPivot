package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill;

import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.OpenTdbClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.QuestionGenerationMode;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Pulls small batches of external trivia samples from OpenTDB and exposes them as evidence.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenTdbQuestionSkill implements AgentSkill<List<AgentEvidenceDTO>> {

    private static final int MAX_EVIDENCE_COUNT = 5;
    private static final int CATEGORY_GENERAL_KNOWLEDGE = 9;
    private static final int CATEGORY_FILM = 11;
    private static final int CATEGORY_MUSIC = 12;
    private static final int CATEGORY_SCIENCE_NATURE = 17;
    private static final int CATEGORY_COMPUTERS = 18;
    private static final int CATEGORY_MATHEMATICS = 19;
    private static final int CATEGORY_SPORTS = 21;
    private static final int CATEGORY_GEOGRAPHY = 22;
    private static final int CATEGORY_HISTORY = 23;

    private final OpenTdbClient openTdbClient;

    @Value("${agent.skills.opentdb.enabled:true}")
    private boolean enabled;

    @Override
    public String name() {
        return "openTdbQuestion";
    }

    @Override
    public List<AgentEvidenceDTO> execute(QuestionAgentContext context) {
        if (!enabled || context == null || context.getRequest() == null) {
            return List.of();
        }

        QuestionGenerateRequestDTO request = context.getRequest();
        if (!QuestionGenerationMode.resolve(request).isPaper()) {
            return List.of();
        }
        if (!supportsQuestionType(request.getQuestionType())) {
            return List.of();
        }

        OpenTdbClient.OpenTdbQuery query = new OpenTdbClient.OpenTdbQuery(
                resolveAmount(request),
                resolveCategoryId(request),
                resolveDifficulty(request.getDifficulty()),
                resolveQuestionType(request.getQuestionType()),
                "url3986"
        );

        try {
            List<OpenTdbClient.TriviaQuestion> questions = openTdbClient.fetchQuestions(query);
            if (CollectionUtils.isEmpty(questions)) {
                return List.of();
            }
            return toEvidenceList(questions, query);
        } catch (RuntimeException ex) {
            log.warn("OpenTDB skill failed, continuing without external trivia. requestId={}, error={}",
                    context.getRequestId(), ex.getMessage());
            return List.of();
        }
    }

    private boolean supportsQuestionType(Integer questionType) {
        return questionType == null
                || questionType == 0
                || questionType == 1
                || questionType == 2
                || questionType == 5;
    }

    private int resolveAmount(QuestionGenerateRequestDTO request) {
        int desired = request != null && request.getQuestionCount() != null ? request.getQuestionCount() : 3;
        return Math.max(1, Math.min(MAX_EVIDENCE_COUNT, desired));
    }

    private String resolveDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty <= 0) {
            return null;
        }
        return switch (difficulty) {
            case 1 -> "easy";
            case 2 -> "medium";
            case 3 -> "hard";
            default -> null;
        };
    }

    private String resolveQuestionType(Integer questionType) {
        if (questionType == null) {
            return null;
        }
        return switch (questionType) {
            case 0, 1 -> "multiple";
            case 2 -> "boolean";
            default -> null;
        };
    }

    private Integer resolveCategoryId(QuestionGenerateRequestDTO request) {
        String text = buildCategoryHintText(request).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(text)) {
            return null;
        }

        if (containsAny(text, "java", "program", "programming", "computer", "software", "algorithm",
                "python", "javascript", "database", "sql", "backend", "frontend", "coding")) {
            return CATEGORY_COMPUTERS;
        }
        if (containsAny(text, "math", "mathematics", "algebra", "geometry", "calculus", "probability",
                "statistics", "linear algebra")) {
            return CATEGORY_MATHEMATICS;
        }
        if (containsAny(text, "physics", "chemistry", "biology", "science", "nature",
                "laboratory", "experiment")) {
            return CATEGORY_SCIENCE_NATURE;
        }
        if (containsAny(text, "history", "historical")) {
            return CATEGORY_HISTORY;
        }
        if (containsAny(text, "geography", "map", "earth")) {
            return CATEGORY_GEOGRAPHY;
        }
        if (containsAny(text, "sport", "sports", "football", "basketball", "soccer")) {
            return CATEGORY_SPORTS;
        }
        if (containsAny(text, "movie", "film", "cinema")) {
            return CATEGORY_FILM;
        }
        if (containsAny(text, "music", "song", "musical")) {
            return CATEGORY_MUSIC;
        }
        if (containsAny(text, "general")) {
            return CATEGORY_GENERAL_KNOWLEDGE;
        }
        return null;
    }

    private String buildCategoryHintText(QuestionGenerateRequestDTO request) {
        StringBuilder builder = new StringBuilder();
        appendHint(builder, request.getPaperName());
        appendHint(builder, request.getPaperType());
        appendHint(builder, request.getRequirement());
        appendHint(builder, joinValues(request.getKnowledgePoints()));
        appendHint(builder, joinValues(request.getAbilityGoals()));
        return builder.toString().trim();
    }

    private void appendHint(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }

    private String joinValues(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return String.join(" ", values);
    }

    private boolean containsAny(String source, String... keywords) {
        if (!StringUtils.hasText(source) || keywords == null || keywords.length == 0) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && source.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private List<AgentEvidenceDTO> toEvidenceList(List<OpenTdbClient.TriviaQuestion> questions,
                                                  OpenTdbClient.OpenTdbQuery query) {
        List<AgentEvidenceDTO> evidences = new ArrayList<>();
        int limit = Math.min(MAX_EVIDENCE_COUNT, questions.size());
        for (int i = 0; i < limit; i++) {
            OpenTdbClient.TriviaQuestion question = questions.get(i);
            AgentEvidenceDTO evidence = new AgentEvidenceDTO();
            evidence.setSourceType("opentdb");
            evidence.setSourceId(buildSourceId(question));
            evidence.setTitle(StringUtils.hasText(question.category()) ? "OpenTDB - " + question.category() : "OpenTDB Sample");
            evidence.setExcerpt(buildExcerpt(question));
            evidence.setScore(0.45D);
            evidence.setMetadata(buildMetadata(question, query));
            evidences.add(evidence);
        }
        return evidences;
    }

    private String buildSourceId(OpenTdbClient.TriviaQuestion question) {
        String raw = (question.question() != null ? question.question() : "")
                + '|'
                + (question.correctAnswer() != null ? question.correctAnswer() : "");
        return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String buildExcerpt(OpenTdbClient.TriviaQuestion question) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(question.question())) {
            builder.append("Question: ").append(question.question().trim());
        }

        List<String> options = new ArrayList<>();
        if (!CollectionUtils.isEmpty(question.incorrectAnswers())) {
            options.addAll(question.incorrectAnswers());
        }
        if (StringUtils.hasText(question.correctAnswer())) {
            options.add(question.correctAnswer().trim());
        }
        if (!options.isEmpty()) {
            builder.append("\nOptions: ").append(String.join(" | ", options));
        }

        if (StringUtils.hasText(question.correctAnswer())) {
            builder.append("\nAnswer: ").append(question.correctAnswer().trim());
        }

        if (StringUtils.hasText(question.difficulty()) || StringUtils.hasText(question.type())) {
            builder.append("\nDifficulty/Type: ")
                    .append(StringUtils.hasText(question.difficulty()) ? question.difficulty().trim() : "unknown")
                    .append(" / ")
                    .append(StringUtils.hasText(question.type()) ? question.type().trim() : "unknown");
        }
        return builder.toString();
    }

    private Map<String, Object> buildMetadata(OpenTdbClient.TriviaQuestion question,
                                              OpenTdbClient.OpenTdbQuery query) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("provider", "OpenTDB");
        metadata.put("providerUrl", "https://opentdb.com/api_config.php");
        metadata.put("category", question.category());
        metadata.put("difficulty", question.difficulty());
        metadata.put("type", question.type());
        metadata.put("correctAnswer", question.correctAnswer());
        metadata.put("incorrectAnswers", question.incorrectAnswers());
        metadata.put("apiCategory", query.category());
        metadata.put("apiDifficulty", query.difficulty());
        metadata.put("apiType", query.type());
        return metadata;
    }
}