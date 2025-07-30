package com.dayz.sapientiacloud_edupivot.student.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "学生信息视图对象 (VO)")
public class StudentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2473367821098855490L;

    @Schema(name = "id", description = "学生ID")
    private UUID id;

    @Schema(name = "studentCode", description = "学号")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "admissionYear", description = "入学年份")
    private Integer admissionYear;

    @Schema(name = "major", description = "专业")
    private String major;

    @Schema(name = "academicStatus", description = "学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)")
    private Integer academicStatus;

    @Schema(name = "status", description = "账号状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "description", description = "自我描述")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    private UUID sysUserId;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}