package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "按范围发送通知数据传输对象")
public class NotificationScopeSendDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "scopeType", description = "发送范围类型 (0=按角色, 1=按课程学生)")
    @NotNull(message = "发送范围类型不能为空")
    @Min(value = 0, message = "发送范围类型输入不正确")
    @Max(value = 1, message = "发送范围类型输入不正确")
    private Integer scopeType;

    @Schema(name = "roleKey", description = "角色标识 (scopeType=0 时必填)")
    @Size(max = 50, message = "角色标识长度不能超过50个字符")
    private String roleKey;

    @Schema(name = "courseId", description = "课程ID (scopeType=1 时必填)")
    private UUID courseId;

    @Schema(name = "title", description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 200, message = "通知标题不能超过200个字符")
    private String title;

    @Schema(name = "content", description = "通知内容（富文本HTML）")
    private String content;

    @Schema(name = "attachmentUrls", description = "附件URL列表（JSON字符串或URL数组，前端与MinIO配合使用）")
    private String attachmentUrls;

    @Schema(name = "type", description = "通知类型 (0=系统通知, 1=课程通知, 2=作业通知, 3=直播通知, 4=其他)")
    @Min(value = 0, message = "通知类型输入不正确")
    @Max(value = 4, message = "通知类型输入不正确")
    private Integer type;

    @Schema(name = "senderId", description = "发送者ID")
    private UUID senderId;

    @Schema(name = "senderName", description = "发送者名称")
    @Size(max = 100, message = "发送者名称不能超过100个字符")
    private String senderName;
}


