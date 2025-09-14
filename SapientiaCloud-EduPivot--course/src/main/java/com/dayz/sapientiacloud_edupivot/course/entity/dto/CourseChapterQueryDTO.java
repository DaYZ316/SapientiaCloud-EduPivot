package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程章节查询数据传输对象 (QueryDTO)")
public class CourseChapterQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "parentId", description = "父章节ID")
    private UUID parentId;

    @Schema(name = "chapterName", description = "章节名称 (支持模糊查询)")
    private String chapterName;
}
