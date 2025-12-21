package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.common.entity.PageEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知查询数据传输对象")
public class NotificationQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "userId", description = "用户ID")
    private UUID userId;

    @Schema(name = "type", description = "通知类型")
    @Min(value = 0, message = "通知类型输入不正确")
    @Max(value = 4, message = "通知类型输入不正确")
    private Integer type;

    @Schema(name = "status", description = "阅读状态 (0=未读, 1=已读)")
    @Min(value = 0, message = "阅读状态输入不正确")
    @Max(value = 1, message = "阅读状态输入不正确")
    private Integer status;

    @Schema(name = "title", description = "通知标题（模糊查询）")
    @Size(max = 200, message = "通知标题不能超过200个字符")
    private String title;

    @Schema(name = "startTime", description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}

