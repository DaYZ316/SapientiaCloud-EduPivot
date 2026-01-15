package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 最终用于保存和返回的出题结果 DTO（对应 ChatMessage.questionResponse 的结构）
 * 包含后期由业务注入的字段，如 id、sysUserId、imageUrls、allowPartialCredit、status 等。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "最终出题结果题目数据传输对象")
public class QuestionResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6552463419472685936L;

    @Schema(name = "id", description = "题目ID（后期落库时注入）")
    private UUID id;

    @Schema(name = "sysUserId", description = "创建用户ID（后期注入，可选）")
    private UUID sysUserId;

    @Schema(name = "requestId", description = "Kafka 请求ID，用于追踪一次出题任务", hidden = true)
    private String requestId;

    @Schema(name = "questionTitle", description = "题目标题")
    private String questionTitle;

    @Schema(name = "questionContent", description = "题目内容")
    private String questionContent;

    @Schema(name = "questionType", description = "题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题)")
    private Integer questionType;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    private Integer difficulty;

    @Schema(name = "score", description = "题目分数")
    private BigDecimal score;

    @Schema(name = "estimatedTime", description = "预计答题时间 (分钟)")
    private Integer estimatedTime;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "options", description = "选项列表 (选择题、判断题使用)")
    @Valid
    private List<QuestionOptionSimpleDTO> options;

    @Schema(name = "answers", description = "答案列表（填空题, 简答题使用）")
    private List<QuestionAnswerSimpleDTO> answers;
}


