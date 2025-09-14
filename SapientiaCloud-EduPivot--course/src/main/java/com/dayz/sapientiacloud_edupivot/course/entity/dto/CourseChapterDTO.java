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
@Schema(description = "课程章节数据传输对象 (DTO)")
public class CourseChapterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "章节ID (更新时必填)")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "parentId", description = "父章节ID (用于支持多级章节结构, NULL表示为一级章节)")
    private UUID parentId;

    @Schema(name = "chapterName", description = "章节名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "章节名称不能为空")
    private String chapterName;

    @Schema(name = "chapterContent", description = "章节内容 (例如: 详细的文本、富文本标记等)")
    private String chapterContent;

    @Schema(name = "sort", description = "章节排序 (值越小越靠前)")
    private Integer sort;
}
