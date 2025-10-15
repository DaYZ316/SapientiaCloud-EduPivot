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
import java.util.UUID;

/**
 * 题目答案数据传输对象
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目答案数据传输对象")
public class QuestionAnswerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 200141681992066454L;

    @Schema(name = "id", description = "答案ID")
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    @NotNull(message = "题目ID不能为空")
    private UUID questionId;

    @Schema(name = "answerContent", description = "答案内容")
    @NotBlank(message = "答案内容不能为空")
    private String answerContent;

    @Schema(name = "answerText", description = "文本答案 (填空题、简答题)")
    private String answerText;

    @Schema(name = "isCorrect", description = "是否正确 (0=错误, 1=正确, 2=部分正确)")
    @NotNull(message = "是否正确不能为空")
    private Integer isCorrect;

    @Schema(name = "score", description = "得分")
    @NotNull(message = "得分不能为空")
    private BigDecimal score;
}
