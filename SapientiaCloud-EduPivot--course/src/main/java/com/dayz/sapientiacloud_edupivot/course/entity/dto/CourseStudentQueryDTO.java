package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程学生分页查询DTO")
public class CourseStudentQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = 8722634881537900052L;

    @Schema(name = "studentId", description = "学生ID", example = "78d44b4a-becd-4f65-9461-f2dcdda03f01")
    private UUID studentId;

    @Schema(name = "courseId", description = "课程ID", example = "78d44b4a-becd-4f65-9461-f2dcdda03f01")
    private UUID courseId;

    @Schema(name = "realName", description = "学生真实姓名", example = "张三")
    private String realName;
}
