package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * 聊天会话查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "聊天会话查询数据传输对象")
public class ChatSessionQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private UUID sysUserId;

    @Schema(description = "会话标题（模糊查询）")
    private String sessionTitle;

    @Schema(description = "会话类型: 0-普通对话, 1-课程问答, 2-题目辅导, 3-知识检索")
    private Integer sessionType;

    @Schema(description = "是否置顶: 0-否, 1-是")
    private Integer isPinned;

    @Schema(description = "是否收藏: 0-否, 1-是")
    private Integer isFavorite;
}
