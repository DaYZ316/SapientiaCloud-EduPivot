package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import lombok.Data;

import java.util.Map;

/**
 * Evidence collected from local tools to guide generation.
 */
@Data
public class AgentEvidenceDTO {

    private String sourceType;
    private String sourceId;
    private String title;
    private String excerpt;
    private Double score;
    private Map<String, Object> metadata;
}
