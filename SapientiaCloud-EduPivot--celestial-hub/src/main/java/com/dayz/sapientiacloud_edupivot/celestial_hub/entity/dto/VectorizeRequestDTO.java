package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 向量化请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "向量化请求")
public class VectorizeRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6174902419812147722L;

    @Schema(description = "课程ID")
    private UUID courseId;

    @NotNull(message = "内容类型不能为空")
    @Schema(description = "内容类型: 0-章节, 1-问题, 2-任务, 3-论坛", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer contentType;

    @Schema(description = "是否强制重新向量化")
    private Boolean forceReindex;

    @Schema(description = "标签过滤")
    private List<String> tags;
}

