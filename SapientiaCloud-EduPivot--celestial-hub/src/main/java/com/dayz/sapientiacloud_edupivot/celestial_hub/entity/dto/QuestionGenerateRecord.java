package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用于引导大模型结构化输出的 Record 类型
 * 仅包含由大模型生成的字段，不包含后期由业务注入的字段（如 id、sysUserId、imageUrls、allowPartialCredit、status）。
 */
public record QuestionGenerateRecord(
        String questionTitle,
        String questionContent,
        Integer questionType,
        Integer difficulty,
        BigDecimal score,
        Integer estimatedTime,
        List<String> tags,
        List<QuestionOptionSimpleDTO> options,
        List<QuestionAnswerSimpleDTO> answers
) {
}


