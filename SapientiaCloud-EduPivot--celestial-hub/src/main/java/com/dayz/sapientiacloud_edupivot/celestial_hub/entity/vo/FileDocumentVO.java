package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件文档视图对象")
public class FileDocumentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7000375221223238792L;

    @Schema(description = "文件ID")
    private UUID id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private Integer fileType;

    @Schema(description = "文件类型名称")
    private String fileTypeName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件大小（格式化）")
    private String fileSizeFormatted;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "存储路径")
    private String storagePath;

    @Schema(description = "上传用户ID")
    private UUID sysUserId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "会话ID")
    private UUID sessionId;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "是否已向量化")
    private Boolean isVectorized;

    @Schema(description = "向量块数量")
    private Integer vectorCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

