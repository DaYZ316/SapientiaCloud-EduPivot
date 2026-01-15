package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "通知数据传输对象")
public class NotificationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "通知ID，更新时必须提供")
    @NotNull(message = "通知ID不能为空")
    private UUID id;

    @Schema(name = "title", description = "通知标题")
    @Size(max = 200, message = "通知标题不能超过200个字符")
    private String title;

    @Schema(name = "content", description = "通知内容（富文本HTML）")
    private String content;

    @Schema(name = "attachmentUrls", description = "附件URL列表（JSON字符串或URL数组，前端与MinIO配合使用）")
    private String attachmentUrls;

    @Schema(name = "type", description = "通知类型")
    private Integer type;
}

