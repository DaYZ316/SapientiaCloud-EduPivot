package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kafka转发AI对话请求")
public class KafkaChatRequestDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID（新对话时可为空）")
    private UUID sessionId;

    @Schema(description = "用户ID")
    private UUID userId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "课程ID（课程相关问答时提供）")
    private UUID courseId;

    @Schema(description = "章节ID（章节相关问答时提供）")
    private UUID chapterId;

    @Schema(description = "会话类型: 0-普通对话, 1-课程问答, 2-题目辅导, 3-知识检索")
    private Integer sessionType;

    @Schema(description = "是否使用RAG检索")
    private Boolean useRag;

    @Schema(description = "是否流式输出")
    private Boolean stream;

    @Schema(description = "温度参数(0.0-1.0)")
    private Double temperature;

    @Schema(description = "最大token数")
    private Integer maxTokens;

    @Schema(description = "附件URL列表")
    private List<String> attachments;
}
