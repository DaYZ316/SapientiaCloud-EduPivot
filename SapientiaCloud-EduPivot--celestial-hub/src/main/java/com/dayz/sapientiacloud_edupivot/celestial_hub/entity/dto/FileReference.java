package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

/**
 * Referenced file metadata used by AI generation requests.
 */
@Data
@Schema(description = "引用文件信息")
public class FileReference {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "66666666-6666-6666-6666-666666666666")
    private UUID id;

    @Schema(description = "文件名", example = "java-basics-review.pdf")
    private String fileName;

    @Schema(description = "文件类型：0-PDF，1-DOC，2-DOCX，3-XLS，4-XLSX，5-TXT，6-MD，7-RTF", example = "0")
    private Integer fileType;

    @Schema(description = "文件大小，单位字节", example = "524288")
    private Long fileSize;

    @Schema(description = "MIME类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "存储路径", example = "course/2026/04/java-basics-review.pdf")
    private String storagePath;

    @Schema(description = "存储桶编码", example = "COURSE_PUBLIC")
    private String bucketCode;

    @Schema(description = "上传用户ID", example = "88888888-8888-8888-8888-888888888888")
    private UUID sysUserId;

    @Schema(description = "关联会话ID", example = "11111111-1111-1111-1111-111111111111")
    private UUID sessionId;

    @Schema(description = "是否已完成向量化", example = "true")
    private Boolean isVectorized;

    @Schema(description = "向量切片数量", example = "16")
    private Integer vectorCount;
}
