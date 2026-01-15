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
import java.util.UUID;

/**
 * AI对话会话实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "mg_chat_session")
@Schema(description = "AI对话会话")
public class ChatSession extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -711905654357771434L;

    @Id
    @Schema(description = "会话ID")
    private UUID id;

    @Field("sys_user_id")
    @Schema(description = "用户ID")
    private UUID sysUserId;

    @Field("session_title")
    @Schema(description = "会话标题")
    private String sessionTitle;

    @Field("session_type")
    @Schema(description = "会话类型: 0-普通对话, 1-课程问答, 2-题目辅导, 3-知识检索")
    private Integer sessionType;

    @Field("is_pinned")
    @Schema(description = "是否置顶: 0-否, 1-是")
    private Integer isPinned;

    @Field("is_favorite")
    @Schema(description = "是否收藏: 0-否, 1-是")
    private Integer isFavorite;

    @BsonIgnore
    @TableField(exist = false)
    private Integer deleted;
}

