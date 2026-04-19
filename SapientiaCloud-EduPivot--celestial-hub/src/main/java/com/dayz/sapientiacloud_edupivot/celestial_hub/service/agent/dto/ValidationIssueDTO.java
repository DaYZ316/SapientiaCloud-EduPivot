package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import lombok.Data;

/**
 * Validation issue raised during generation or repair.
 */
@Data
public class ValidationIssueDTO {

    private String code;
    private String level;
    private String message;
    private Integer questionIndex;
    private String repairHint;
}
