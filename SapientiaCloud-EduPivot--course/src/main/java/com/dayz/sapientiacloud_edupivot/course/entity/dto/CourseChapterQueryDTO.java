package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程章节查询数据传输对象")
public class CourseChapterQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6330894516542152346L;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "chapterName", description = "章节名称（模糊查询）")
    private String chapterName;

    @Schema(name = "parentChapterId", description = "父章节ID")
    private UUID parentChapterId;

    @Schema(name = "status", description = "章节状态 (0=草稿, 1=发布, 2=下架)")
    private Integer status;

    @Schema(name = "minViewCount", description = "最小浏览次数")
    private Long minViewCount;

    @Schema(name = "maxViewCount", description = "最大浏览次数")
    private Long maxViewCount;
}
