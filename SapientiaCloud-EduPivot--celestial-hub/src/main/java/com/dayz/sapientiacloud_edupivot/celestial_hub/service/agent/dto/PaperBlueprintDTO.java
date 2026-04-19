package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Global plan for the current generation request.
 */
@Data
public class PaperBlueprintDTO {

    private String blueprintId;
    private String generationStrategy;
    private Integer totalQuestionCount;
    private BigDecimal totalScore;
    private Integer totalEstimatedTime;
    private List<PaperSectionPlanDTO> sections = new ArrayList<>();
}
