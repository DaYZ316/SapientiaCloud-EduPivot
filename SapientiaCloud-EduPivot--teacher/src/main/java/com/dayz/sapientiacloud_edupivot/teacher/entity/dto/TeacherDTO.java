package com.dayz.sapientiacloud_edupivot.teacher.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "教师信息数据传输对象")
public class TeacherDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2981841374381337323L;

    @Schema(name = "id", description = "教师ID，更新时必须提供")
    private UUID id;

    @Schema(name = "teacherCode", description = "教师工号")
    @NotBlank(message = "教师工号不能为空")
    @Size(max = 20, message = "教师工号不能超过20个字符")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "department", description = "所属部门/学院")
    @Size(max = 100, message = "所属部门不能超过100个字符")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    @Min(value = 0, message = "学历输入不正确")
    @Max(value = 3, message = "学历输入不正确")
    private Integer education;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    @Size(max = 200, message = "专业特长不能超过200个字符")
    private String specialization;

    @Schema(name = "status", description = "账号状态 (0=正常, 1=停用)")
    @Min(value = 0, message = "状态输入不正确")
    @Max(value = 1, message = "状态输入不正确")
    private Integer status;

    @Schema(name = "description", description = "自我描述")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    private UUID sysUserId;
}