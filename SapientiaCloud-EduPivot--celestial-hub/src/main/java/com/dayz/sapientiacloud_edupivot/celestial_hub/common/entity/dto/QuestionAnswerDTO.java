package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign DTO for persisting question answers in the course service.
 */
@Data
public class QuestionAnswerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 200141681992066454L;

    private UUID id;
    private UUID questionId;
    private UUID courseId;
    private String answerContent;
    private String explanation;
    private BigDecimal score;
    private Integer sortOrder;
}
