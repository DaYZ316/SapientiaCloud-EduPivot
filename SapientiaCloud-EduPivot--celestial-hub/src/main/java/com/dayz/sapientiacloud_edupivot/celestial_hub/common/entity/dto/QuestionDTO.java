package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Feign DTO for persisting generated questions in the course service.
 */
@Data
public class QuestionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6552463419472685936L;

    private UUID id;
    private UUID questionBankId;
    private UUID courseId;
    private UUID sysUserId;
    private String questionTitle;
    private String questionContent;
    private Integer questionType;
    private Integer difficulty;
    private BigDecimal score;
    private Integer estimatedTime;
    private List<String> tags;
    private List<String> imageUrls;
    private Integer allowPartialCredit;
    private Integer status;
    private List<QuestionOptionDTO> options;
    private List<QuestionAnswerDTO> answers;
    private UUID celestialQuestionId;
}
