package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Explicitly distinguishes simple question generation from full paper generation.
 */
public enum QuestionGenerationMode {

    QUESTION("question"),
    PAPER("paper");

    private final String code;

    QuestionGenerationMode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isPaper() {
        return this == PAPER;
    }

    public static QuestionGenerationMode fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        for (QuestionGenerationMode value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return value;
            }
        }
        return null;
    }

    public static QuestionGenerationMode resolve(QuestionGenerateRequestDTO request) {
        if (request == null) {
            return QUESTION;
        }

        QuestionGenerationMode explicitMode = fromCode(request.getGenerationMode());
        if (explicitMode != null) {
            return explicitMode;
        }

        boolean paperLikeRequest = StringUtils.hasText(request.getPaperName())
                || StringUtils.hasText(request.getPaperType())
                || request.getTotalScore() != null
                || request.getTotalEstimatedTime() != null
                || !CollectionUtils.isEmpty(request.getKnowledgePoints())
                || !CollectionUtils.isEmpty(request.getAbilityGoals());
        return paperLikeRequest ? PAPER : QUESTION;
    }
}
