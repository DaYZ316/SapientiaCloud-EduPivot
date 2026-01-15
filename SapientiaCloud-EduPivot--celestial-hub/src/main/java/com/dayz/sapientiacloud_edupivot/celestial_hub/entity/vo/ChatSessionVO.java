package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "AI对话会话")
public class ChatSessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4090429515130422068L;

    @Schema(description = "会话ID")
    private UUID id;

    @Schema(description = "用户ID")
    private UUID sysUserId;

    @Schema(description = "会话标题")
    private String sessionTitle;

    @Schema(description = "会话类型")
    private Integer sessionType;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "是否置顶")
    private Integer isPinned;

    @Schema(description = "是否收藏")
    private Integer isFavorite;

    @Schema(description = "最后一条消息预览")
    private String lastMessagePreview;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

