package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Feign DTO for persisting question options in the course service.
 */
@Data
public class QuestionOptionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4682064944496246429L;

    private UUID id;
    private UUID questionId;
    private UUID courseId;
    private String optionContent;
    private String optionLabel;
    private Integer isCorrect;
    private BigDecimal score;
    private List<String> imageUrls;
    private String explanation;
}
