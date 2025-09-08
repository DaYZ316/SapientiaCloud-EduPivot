package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程教师团队分配数据传输对象")
public class CourseTeacherTeamDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "courseId", description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "teacherIds", description = "教师ID列表")
    @NotNull(message = "教师ID列表不能为空")
    private List<UUID> teacherIds;
}
