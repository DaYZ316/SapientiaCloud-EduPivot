package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * 文件向量化请求DTO（用于Kafka消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件向量化请求")
public class FileVectorizeRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID fileId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件类型")
    private Integer fileType;

    @Schema(description = "存储路径")
    private String storagePath;

    @Schema(description = "桶编码")
    private String bucketCode;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "会话ID")
    private UUID sessionId;

    @Schema(description = "用户ID")
    private UUID userId;
}

