package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import lombok.Data;

/**
 * Incremental process detail emitted during question or paper generation.
 */
@Data
public class QuestionGenerationTraceEntryDTO {

    private String entryId;
    private String stage;
    private String source;
    private String detailType;
    private String title;
    private String summary;
    private Object payload;
    private Long timestamp;
}
