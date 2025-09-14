package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程论坛主贴数据传输对象 (DTO)")
public class CourseThreadDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "主贴ID (更新时必填)")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "userId", description = "发帖用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private UUID userId;

    @Schema(name = "title", description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "帖子标题不能为空")
    private String title;

    @Schema(name = "content", description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    @Schema(name = "isPinned", description = "是否置顶 (1=是, 0=否)")
    private Integer pinned;

    @Schema(name = "isClosed", description = "是否关闭/锁定 (1=是, 0=否)")
    private Integer closed;
}
