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
@Schema(description = "课程论坛主贴查询数据传输对象 (QueryDTO)")
public class CourseThreadQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "userId", description = "发帖用户ID")
    private UUID userId;

    @Schema(name = "title", description = "帖子标题 (支持模糊查询)")
    private String title;

    @Schema(name = "isPinned", description = "是否置顶 (1=是, 0=否)")
    private Integer pinned;

    @Schema(name = "isClosed", description = "是否关闭/锁定 (1=是, 0=否)")
    private Integer closed;
}
