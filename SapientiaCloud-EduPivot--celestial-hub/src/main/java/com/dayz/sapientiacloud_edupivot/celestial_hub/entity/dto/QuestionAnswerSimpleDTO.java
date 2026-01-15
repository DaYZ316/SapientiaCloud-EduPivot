package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * AI出题结果中使用的答案结构（去掉与课程绑定的字段）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "AI出题结果答案DTO")
public class QuestionAnswerSimpleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 200141681992066454L;

    @Schema(name = "id", description = "答案ID")
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    private UUID questionId;

    @Schema(name = "answerContent", description = "本空答案")
    private String answerContent;

    @Schema(name = "explanation", description = "本空解析")
    private String explanation;

    @Schema(name = "score", description = "分数")
    private BigDecimal score;

    @Schema(name = "sortOrder", description = "本空序号")
    private Integer sortOrder;
}


