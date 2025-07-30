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
@Schema(description = "学生详细信息视图对象 (包含系统用户信息)")
public class StudentDetailVO implements Serializable {

    // TODO

    @Serial
    private static final long serialVersionUID = -2832136955242675495L;

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

    @Schema(name = "nickName", description = "用户昵称")
    private String nickName;

    @Schema(name = "email", description = "邮箱")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    private String mobile;

    @Schema(name = "gender", description = "性别 (0=未知, 1=男, 2=女)")
    private Integer gender;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "lastLoginTime", description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}