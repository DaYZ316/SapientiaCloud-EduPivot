package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件上传请求DTO")
public class FileUploadDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7000375221223238792L;

    @Schema(description = "课程ID（可选）")
    private UUID courseId;

    @Schema(description = "会话ID（可选，关联会话）")
    private UUID sessionId;

    @Schema(description = "是否自动向量化")
    private Boolean autoVectorize;
}

