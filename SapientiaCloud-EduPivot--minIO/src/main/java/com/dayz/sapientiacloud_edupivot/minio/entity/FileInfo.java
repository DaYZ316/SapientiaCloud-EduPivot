package com.dayz.sapientiacloud_edupivot.minio.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件信息DTO
 *
 * @author LANDH
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件详细信息")
public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -2783247409318597554L;

    @Schema(description = "文件对象名称", example = "2024/01/15/example.jpg")
    private String objectName;

    @Schema(description = "文件名", example = "example.jpg")
    private String fileName;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long size;

    @Schema(description = "文件内容类型", example = "image/jpeg")
    private String contentType;

    @Schema(description = "最后修改时间")
    private LocalDateTime lastModified;

    @Schema(description = "ETag", example = "\"d41d8cd98f00b204e9800998ecf8427e\"")
    private String etag;

    @Schema(description = "是否为目录", example = "false")
    private Boolean isDir;

    @Schema(description = "文件访问URL", example = "http://localhost:9000/bucket/2024/01/15/example.jpg")
    private String url;

    @Schema(description = "文件扩展名", example = ".jpg")
    private String extension;

    @Schema(description = "文件路径（不包含文件名）", example = "2024/01/15/")
    private String path;

    @Schema(description = "存储桶名称", example = "edupivot-files")
    private String bucketName;

    @Schema(description = "业务桶编码", example = "COURSE_PUBLIC")
    private String bucketCode;

    @Schema(description = "是否有错误", example = "false")
    private Boolean error;

    @Schema(description = "错误信息", example = "文件不存在")
    private String errorMessage;
}
