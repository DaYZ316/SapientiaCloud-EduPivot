package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * 文件引用信息（对应 mg_file_document 中的属性）
 */
@Data
@Schema(description = "文件引用信息")
public class FileReference {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型: 0-PDF, 1-DOC, 2-DOCX, 3-XLS, 4-XLSX, 5-TXT, 6-MD, 7-RTF")
    private Integer fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "存储路径（MinIO对象名）")
    private String storagePath;

    @Schema(description = "存储桶代码")
    private String bucketCode;

    @Schema(description = "上传用户ID")
    private UUID sysUserId;

    @Schema(description = "会话ID（可选，关联会话）")
    private UUID sessionId;

    @Schema(description = "是否已向量化")
    private Boolean isVectorized;

    @Schema(description = "向量块数量")
    private Integer vectorCount;
}


