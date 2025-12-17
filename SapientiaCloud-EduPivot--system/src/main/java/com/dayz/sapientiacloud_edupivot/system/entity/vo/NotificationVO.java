package com.dayz.sapientiacloud_edupivot.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "通知视图对象 (VO)")
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "通知ID")
    private UUID id;

    @Schema(name = "userId", description = "接收用户ID")
    private UUID userId;

    @Schema(name = "title", description = "通知标题")
    private String title;

    @Schema(name = "content", description = "通知内容（富文本HTML）")
    private String content;

    @Schema(name = "attachmentUrls", description = "附件URL列表（JSON字符串或URL数组，前端与MinIO配合使用）")
    private String attachmentUrls;

    @Schema(name = "type", description = "通知类型 (0=系统通知, 1=课程通知, 2=作业通知, 3=直播通知, 4=其他)")
    private Integer type;

    @Schema(name = "typeLabel", description = "通知类型标签")
    private String typeLabel;

    @Schema(name = "status", description = "阅读状态 (0=未读, 1=已读)")
    private Integer status;

    @Schema(name = "statusLabel", description = "阅读状态标签")
    private String statusLabel;

    @Schema(name = "readTime", description = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @Schema(name = "senderId", description = "发送者ID")
    private UUID senderId;

    @Schema(name = "senderName", description = "发送者名称")
    private String senderName;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

