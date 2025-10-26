package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo;

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
    private static final long serialVersionUID = 8521685108180660843L;

    @Schema(name = "id", description = "学生ID")
    private UUID id;

    @Schema(name = "studentCode", description = "学生学号")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "major", description = "专业")
    private String major;

    @Schema(name = "grade", description = "年级")
    private String grade;

    @Schema(name = "class", description = "班级")
    private String className;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    private Integer education;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    private String specialization;

    @Schema(name = "description", description = "自我描述")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    private UUID sysUserId;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "username", description = "用户名")
    private String username;

    @Schema(name = "nickName", description = "用户昵称")
    private String nickName;

    @Schema(name = "email", description = "邮箱")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    private String mobile;

    @Schema(name = "gender", description = "性别 (0=女, 1=男)")
    private Integer gender;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "lastLoginTime", description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}
