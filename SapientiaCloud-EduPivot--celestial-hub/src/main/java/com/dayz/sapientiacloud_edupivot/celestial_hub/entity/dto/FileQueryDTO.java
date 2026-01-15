package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件查询DTO")
public class FileQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7000375221223238792L;

    @Schema(description = "会话ID")
    private UUID sessionId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "用户ID")
    private UUID userId;

    @Schema(description = "文件类型列表")
    private List<Integer> fileTypes;

    @Schema(description = "状态列表")
    private List<Integer> statuses;

    @Schema(description = "是否已向量化")
    private Boolean isVectorized;

    @Schema(description = "文件名（模糊查询）")
    private String fileName;
}

