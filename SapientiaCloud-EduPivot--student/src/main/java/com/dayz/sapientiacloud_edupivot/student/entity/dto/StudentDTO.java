package com.dayz.sapientiacloud_edupivot.student.entity.dto;

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
@Schema(description = "学生信息数据传输对象")
public class StudentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5542707386259350792L;

    @Schema(name = "id", description = "学生ID，更新时必须提供")
    private UUID id;

    @Schema(name = "studentCode", description = "学号")
    @NotBlank(message = "学号不能为空")
    @Size(max = 20, message = "学号不能超过20个字符")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "admissionYear", description = "入学年份")
    @Min(value = 1900, message = "入学年份不能小于1900年")
    @Max(value = 9999, message = "入学年份不能大于9999年")
    private Integer admissionYear;

    @Schema(name = "major", description = "专业")
    @Size(max = 100, message = "专业不能超过100个字符")
    private String major;

    @Schema(name = "academicStatus", description = "学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)")
    @Min(value = 0, message = "学籍状态输入不正确")
    @Max(value = 3, message = "学籍状态输入不正确")
    private Integer academicStatus;

    @Schema(name = "description", description = "自我描述")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    private UUID sysUserId;
}