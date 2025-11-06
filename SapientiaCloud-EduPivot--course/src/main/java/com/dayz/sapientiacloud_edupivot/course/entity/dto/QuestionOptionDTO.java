package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 题目选项数据传输对象
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目选项数据传输对象")
public class QuestionOptionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4682064944496246429L;

    @Schema(name = "id", description = "选项ID")
    private UUID id;

    @Schema(name = "questionId", description = "所属题目ID")
    private UUID questionId;

    @Schema(name = "optionContent", description = "选项内容")
    @NotBlank(message = "选项内容不能为空")
    private String optionContent;

    @Schema(name = "optionLabel", description = "选项标签 (A, B, C, D等)")
    @NotBlank(message = "选项标签不能为空")
    private String optionLabel;

    @Schema(name = "isCorrect", description = "是否为正确答案 (0=错误, 1=正确)")
    @NotNull(message = "是否正确答案不能为空")
    private Integer isCorrect;

    @Schema(name = "score", description = "选项分数 (多选题部分得分使用)")
    private BigDecimal score;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "explanation", description = "选项解析")
    private String explanation;
}
