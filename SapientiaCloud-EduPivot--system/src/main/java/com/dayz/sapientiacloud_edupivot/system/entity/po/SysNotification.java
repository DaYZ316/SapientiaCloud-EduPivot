package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.system.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_notification")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统通知持久化对象 (PO)")
public class SysNotification extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "通知ID")
    @TableId("id")
    private UUID id;

    @Schema(name = "userId", description = "接收用户ID")
    @TableField("user_id")
    private UUID userId;

    @Schema(name = "title", description = "通知标题")
    @TableField("title")
    private String title;

    @Schema(name = "content", description = "通知内容（富文本HTML）")
    @TableField("content")
    private String content;

    @Schema(name = "attachmentUrls", description = "附件URL列表（JSON字符串或逗号分隔）")
    @TableField("attachment_urls")
    private String attachmentUrls;

    @Schema(name = "type", description = "通知类型 (0=系统通知, 1=课程通知, 2=作业通知, 3=直播通知, 4=其他)")
    @TableField("type")
    private Integer type;

    @Schema(name = "status", description = "阅读状态 (0=未读, 1=已读)")
    @TableField("status")
    private Integer status;

    @Schema(name = "readTime", description = "阅读时间")
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @Schema(name = "senderId", description = "发送者ID")
    @TableField("sender_id")
    private UUID senderId;

    @Schema(name = "senderName", description = "发送者名称")
    @TableField("sender_name")
    private String senderName;
}

