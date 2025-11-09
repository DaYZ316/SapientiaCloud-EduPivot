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

/**
 * 知识检索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识检索请求")
public class KnowledgeRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -155310913868961743L;

    @NotBlank(message = "查询内容不能为空")
    @Schema(description = "查询内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "章节ID")
    private UUID chapterId;

    @Schema(description = "内容类型过滤: 0-章节, 1-问题, 2-答案, 3-论坛")
    private List<Integer> contentTypes;

    @Schema(description = "返回结果数量")
    private Integer topK;

    @Schema(description = "相似度阈值(0.0-1.0)")
    private Double similarityThreshold;

    @Schema(description = "标签过滤")
    private List<String> tags;
}

