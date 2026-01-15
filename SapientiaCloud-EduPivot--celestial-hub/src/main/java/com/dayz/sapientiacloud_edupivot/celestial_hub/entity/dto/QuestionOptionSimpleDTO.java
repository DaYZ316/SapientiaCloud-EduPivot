package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * AI出题结果中使用的选项结构（去掉与课程绑定的字段）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "AI出题结果选项DTO")
public class QuestionOptionSimpleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4682064944496246429L;

    @Schema(name = "id", description = "选项ID")
    private UUID id;

    @Schema(name = "questionId", description = "所属题目ID")
    private UUID questionId;

    @Schema(name = "optionContent", description = "选项内容")
    private String optionContent;

    @Schema(name = "optionLabel", description = "选项标签 (A, B, C, D等)")
    private String optionLabel;

    @Schema(name = "isCorrect", description = "是否为正确答案 (0=错误, 1=正确)")
    private Integer isCorrect;

    @Schema(name = "score", description = "选项分数 (多选题部分得分使用)")
    private BigDecimal score;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "explanation", description = "选项解析")
    private String explanation;
}


