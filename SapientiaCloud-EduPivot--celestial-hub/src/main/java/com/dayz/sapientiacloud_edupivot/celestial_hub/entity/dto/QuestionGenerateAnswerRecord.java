package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Model-only answer record used for structured AI generation output.
 * Business identifiers are intentionally excluded so invalid AI-generated UUIDs
 * cannot break deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionGenerateAnswerRecord(
        @JsonAlias({"AnswerContent"}) String answerContent,
        @JsonAlias({"Explanation"}) String explanation,
        @JsonAlias({"Score"}) BigDecimal score,
        @JsonAlias({"SortOrder"}) Integer sortOrder
) {
}
