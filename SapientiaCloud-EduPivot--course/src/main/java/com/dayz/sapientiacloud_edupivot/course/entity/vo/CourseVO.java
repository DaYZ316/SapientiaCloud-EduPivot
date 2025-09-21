package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程视图对象 (VO)")
public class CourseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6827896902246157143L;

    @Schema(name = "id", description = "课程ID")
    private UUID id;

    @Schema(name = "courseName", description = "课程名称")
    private String courseName;

    @Schema(name = "description", description = "课程描述")
    private String description;

    @Schema(name = "courseType", description = "课程类型 (0=必修, 1=选修)")
    private Integer courseType;

    @Schema(name = "semester", description = "开设学期")
    private String semester;

    @Schema(name = "location", description = "上课地点")
    private String location;

    @Schema(name = "teacherId", description = "授课教师ID")
    private UUID teacherId;

    @Schema(name = "teacherName", description = "授课教师姓名")
    private String teacherName;

    @Schema(name = "teacherAvatar", description = "授课教师头像")
    private String teacherAvatar;

    @Schema(name = "assistantTeacherIds", description = "辅助教学教师ID列表")
    private List<UUID> assistantTeacherIds;

    @Schema(name = "coverImageUrl", description = "课程封面图片URL")
    private String coverImageUrl;

    @Schema(name = "status", description = "课程状态 (0=正常, 1=停课)", example = "0")
    private Integer status;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
