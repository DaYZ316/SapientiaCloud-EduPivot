package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * AI 出题请求 DTO
 * 注意：不包含 courseId 与 questionBankId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI出题请求参数")
public class QuestionGenerateRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2153417259123456789L;

    @Schema(description = "会话ID（可选，若为空则由后端创建新的出题会话）")
    private UUID sessionId;

    @Schema(description = "生成题目数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生成题目数量不能为空")
    @Min(value = 1, message = "生成题目数量不能小于1")
    @Max(value = 100, message = "生成题目数量不能大于100")
    private Integer questionCount;

    @Schema(description = "题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题, 5=混合出题)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目类型不能为空")
    private Integer questionType;

    @Schema(description = "难度等级 (0=随机, 1=简单, 2=中等, 3=困难)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "难度等级不能为空")
    private Integer difficulty;

    @Schema(description = "每题分数，若为空则由AI统一或按难度分配")
    private BigDecimal scorePerQuestion;

    @Schema(description = "出题详细要求（题型组合、考查能力、场景限制等）")
    @Size(max = 1000, message = "出题要求不能超过1000个字符")
    private String requirement;
}

