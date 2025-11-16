package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识项VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "知识项")
public class KnowledgeItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4340269952424819567L;

    @Schema(description = "内容ID")
    private UUID id;

    @Schema(description = "内容类型: 0-章节, 1-问题, 2-答案, 3-论坛")
    private Integer contentType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "相似度分数")
    private Double score;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "章节ID")
    private UUID chapterId;

    @Schema(description = "题库ID（问题类型时使用）")
    private UUID questionBankId;

    @Schema(description = "问题ID（问题类型时使用）")
    private UUID questionId;

    @Schema(description = "任务ID（任务类型时使用）")
    private UUID taskId;

    @Schema(description = "论坛ID（论坛类型时使用）")
    private UUID forumId;

    @Schema(description = "帖子ID（论坛类型时使用）")
    private UUID postId;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;
}

