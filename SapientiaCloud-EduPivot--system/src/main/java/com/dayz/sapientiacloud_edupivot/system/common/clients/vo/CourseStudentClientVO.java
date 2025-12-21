package com.dayz.sapientiacloud_edupivot.system.common.clients.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "课程学生简要视图对象 (Feign 客户端)")
public class CourseStudentClientVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "studentId", description = "学生ID")
    private UUID studentId;

    @Schema(name = "sysUserId", description = "系统用户ID")
    private UUID sysUserId;

    @Schema(name = "courseId", description = "课程ID")
    private UUID courseId;

    @Schema(name = "realName", description = "学生真实姓名")
    private String realName;
}


