package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_notification_user")
@Schema(description = "系统通知用户关联状态 (PO)")
public class SysNotificationUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "收件记录ID")
    @TableId("id")
    private UUID id;

    @Schema(name = "notificationId", description = "通知消息ID")
    @TableField("notification_id")
    private UUID notificationId;

    @Schema(name = "userId", description = "接收用户ID")
    @TableField("user_id")
    private UUID userId;

    @Schema(name = "status", description = "阅读状态 (0=未读, 1=已读)")
    @TableField("status")
    private Integer status;

    @Schema(name = "readTime", description = "阅读时间")
    @TableField("read_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @Schema(name = "createTime", description = "入箱时间")
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
