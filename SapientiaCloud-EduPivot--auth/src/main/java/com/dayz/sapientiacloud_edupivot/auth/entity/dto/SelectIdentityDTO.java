package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "身份选择数据传输对象")
public class SelectIdentityDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "identityType", description = "身份类型 (student=学生, teacher=教师)", example = "student", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identityType;

    @Schema(name = "studentInfo", description = "学生信息（当身份类型为student时必填）")
    @Valid
    private StudentAddDTO studentInfo;

    @Schema(name = "teacherInfo", description = "教师信息（当身份类型为teacher时必填）")
    @Valid
    private TeacherAddDTO teacherInfo;
}

