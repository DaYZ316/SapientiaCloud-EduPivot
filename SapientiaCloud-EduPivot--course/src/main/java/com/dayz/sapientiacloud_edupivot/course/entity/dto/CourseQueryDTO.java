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
@Schema(description = "课程查询数据传输对象")
public class CourseQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7756280467062573715L;

    @Schema(name = "courseName", description = "课程名称（模糊查询）")
    private String courseName;

    @Schema(name = "teacherId", description = "授课教师ID")
    private UUID teacherId;

    @Schema(name = "semester", description = "学期")
    private String semester;

    @Schema(name = "courseType", description = "课程类型")
    private Integer courseType;

    @Schema(name = "location", description = "上课地点")
    private String location;

    @Schema(name = "status", description = "课程状态 (0=正常, 1=停课)")
    private Integer status;

    @Schema(name = "studentId", description = "学生ID")
    private UUID studentId;

    @Schema(name = "isPublic", description = "是否公开 (0=仅课程成员, 1=公开)")
    private Integer isPublic;
}
