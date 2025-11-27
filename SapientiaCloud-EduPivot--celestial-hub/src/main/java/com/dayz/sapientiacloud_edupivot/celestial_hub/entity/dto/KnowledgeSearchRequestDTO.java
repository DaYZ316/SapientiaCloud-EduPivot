package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * 知识检索请求
 */
@Data
@Schema(description = "知识检索请求")
public class KnowledgeSearchRequestDTO {

    @NotBlank
    @Schema(description = "检索查询内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "返回结果数量（TopK）")
    private Integer topK;

    @Schema(description = "相似度阈值")
    private Double similarityThreshold;

    @Schema(description = "会话ID")
    private UUID sessionId;
}


