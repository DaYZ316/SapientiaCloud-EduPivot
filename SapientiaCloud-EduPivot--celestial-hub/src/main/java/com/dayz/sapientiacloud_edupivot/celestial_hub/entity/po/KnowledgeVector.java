package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识向量实体（用于RAG）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "mg_knowledge_vector")
@Schema(description = "知识向量")
public class KnowledgeVector extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -7809471544065509632L;

    @Id
    @Schema(description = "向量ID")
    private UUID id;

    @Field("course_id")
    @Schema(description = "课程ID")
    private UUID courseId;

    @Field("chapter_id")
    @Schema(description = "章节ID")
    private UUID chapterId;

    @Field("question_bank_id")
    @Schema(description = "题库ID（问题类型时使用）")
    private UUID questionBankId;

    @Field("question_id")
    @Schema(description = "问题ID（问题类型时使用）")
    private UUID questionId;

    @Field("task_id")
    @Schema(description = "任务ID（任务类型时使用）")
    private UUID taskId;

    @Field("forum_id")
    @Schema(description = "论坛ID（论坛类型时使用）")
    private UUID forumId;

    @Field("post_id")
    @Schema(description = "帖子ID（论坛类型时使用）")
    private UUID postId;

    @Field("content_type")
    @Schema(description = "内容类型: 0-章节, 1-问题, 2-任务, 3-论坛")
    private Integer contentType;

    @Field("content_id")
    @Schema(description = "内容ID")
    private UUID contentId;

    @Field("title")
    @Schema(description = "标题")
    private String title;

    @Field("content")
    @Schema(description = "文本内容")
    private String content;

    @Field("vector_id")
    @Schema(description = "向量存储ID（Redis向量库）")
    private String vectorId;

    @Field("embedding_model")
    @Schema(description = "嵌入模型名称")
    private String embeddingModel;

    @Field("tags")
    @Schema(description = "标签列表")
    private List<String> tags;

    @Field("metadata")
    @Schema(description = "元数据")
    private Map<String, Object> metadata;

    @Field("status")
    @Schema(description = "状态: 0-正常, 1-已失效")
    private Integer status;

    @BsonIgnore
    @TableField(exist = false)
    private Integer deleted;
}

