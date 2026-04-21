package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Top-level structured output for Spring AI question generation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionGeneratePayload(
        List<QuestionGenerateRecord> questions
) {

    public List<QuestionGenerateRecord> questionsOrEmpty() {
        return questions != null ? questions : List.of();
    }
}
