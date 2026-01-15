package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "公开课程视图对象 (VO)")
public class PublicCourseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "id", description = "课程ID")
    private UUID id;

    @Schema(name = "courseName", description = "课程名称")
    private String courseName;

    @Schema(name = "description", description = "课程描述")
    private String description;

    @Schema(name = "coverImageUrl", description = "课程封面图片URL")
    private String coverImageUrl;

    @Schema(name = "courseType", description = "课程类型 (0=必修, 1=选修)")
    private Integer courseType;

    @Schema(name = "semester", description = "开设学期")
    private String semester;

    @Schema(name = "location", description = "上课地点")
    private String location;

    @Schema(name = "teacherName", description = "授课教师姓名")
    private String teacherName;

    @Schema(name = "teacherAvatar", description = "授课教师头像")
    private String teacherAvatar;
}

