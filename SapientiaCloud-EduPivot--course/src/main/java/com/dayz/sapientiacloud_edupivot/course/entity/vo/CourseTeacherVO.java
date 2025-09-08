package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课程教师团队关联信息视图对象 (VO)")
public class CourseTeacherVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3855290559752284192L;

    @Schema(name = "courseId", description = "课程ID")
    private UUID courseId;

    @Schema(name = "courseName", description = "课程名称")
    private String courseName;

    @Schema(name = "teacherId", description = "教师ID")
    private UUID teacherId;

    @Schema(name = "teacherName", description = "教师姓名")
    private String teacherName;

    @Schema(name = "roleType", description = "角色类型 (0=负责人, 1=教学团队成员)")
    private Integer roleType;

    @Schema(name = "roleTypeName", description = "角色类型名称")
    private String roleTypeName;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
