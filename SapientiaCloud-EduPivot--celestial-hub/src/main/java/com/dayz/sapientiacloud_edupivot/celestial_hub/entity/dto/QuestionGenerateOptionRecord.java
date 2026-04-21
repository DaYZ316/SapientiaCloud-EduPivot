package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Model-only option record used for structured AI generation output.
 * Business identifiers are intentionally excluded so invalid AI-generated UUIDs
 * cannot break deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionGenerateOptionRecord(
        @JsonAlias({"OptionContent"}) String optionContent,
        @JsonAlias({"OptionLabel"}) String optionLabel,
        @JsonAlias({"IsCorrect"}) Integer isCorrect,
        @JsonAlias({"Score"}) BigDecimal score,
        @JsonAlias({"ImageUrls"}) List<String> imageUrls,
        @JsonAlias({"Explanation"}) String explanation
) {
}
