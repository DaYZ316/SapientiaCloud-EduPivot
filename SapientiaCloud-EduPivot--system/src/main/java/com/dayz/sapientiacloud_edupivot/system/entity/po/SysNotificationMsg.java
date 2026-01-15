package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.system.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_notification_msg")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统通知消息内容 (PO)")
public class SysNotificationMsg extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "通知ID")
    @TableId("id")
    private UUID id;

    @Schema(name = "title", description = "通知标题")
    @TableField("title")
    private String title;

    @Schema(name = "content", description = "通知内容（富文本HTML）")
    @TableField("content")
    private String content;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    @TableField("attachment_urls")
    private String attachmentUrls;

    @Schema(name = "type", description = "通知类型")
    @TableField("type")
    private Integer type;

    @Schema(name = "senderId", description = "发送者ID")
    @TableField("sender_id")
    private UUID senderId;

    @Schema(name = "senderName", description = "发送者名称")
    @TableField("sender_name")
    private String senderName;
}
