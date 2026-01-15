package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 知识检索结果
 */
@Data
@Schema(description = "知识检索结果")
public class KnowledgeSearchResultVO {

    @Schema(description = "向量ID")
    private String vectorId;

    @Schema(description = "向量存储ID（Redis）")
    private String documentId;

    @Schema(description = "内容类型")
    private Integer contentType;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容片段")
    private String content;

    @Schema(description = "分数")
    private Double score;

    @Schema(description = "距离")
    private Double distance;

    @Schema(description = "Chunk索引")
    private Integer chunkIndex;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "章节ID")
    private UUID chapterId;

    @Schema(description = "内容ID")
    private UUID contentId;

    @Schema(description = "题库ID")
    private UUID questionBankId;

    @Schema(description = "问题ID")
    private UUID questionId;

    @Schema(description = "任务ID")
    private UUID taskId;

    @Schema(description = "论坛ID")
    private UUID forumId;

    @Schema(description = "帖子ID")
    private UUID postId;

    @Schema(description = "文件ID")
    private UUID fileId;

    @Schema(description = "会话ID")
    private UUID sessionId;

    @Schema(description = "聊天消息ID")
    private UUID messageId;

    @Schema(description = "用户ID")
    private UUID userId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "嵌入模型")
    private String embeddingModel;
}


