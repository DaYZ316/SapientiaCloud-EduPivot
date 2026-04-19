package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-section generation plan.
 */
@Data
public class PaperSectionPlanDTO {

    private Integer sectionNo;
    private String sectionTitle;
    private Integer questionType;
    private Integer difficulty;
    private Integer targetCount;
    private BigDecimal scorePerQuestion;
    private Integer estimatedTimePerQuestion;
    private Integer totalEstimatedTime;
    private List<String> knowledgePoints = new ArrayList<>();
    private List<AgentEvidenceDTO> evidences = new ArrayList<>();
}
