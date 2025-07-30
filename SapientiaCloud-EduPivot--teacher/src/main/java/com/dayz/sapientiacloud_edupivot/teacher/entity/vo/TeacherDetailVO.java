package com.dayz.sapientiacloud_edupivot.teacher.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "教师详细信息视图对象 (VO)")
public class TeacherDetailVO implements Serializable {

    //TODO

    @Serial
    private static final long serialVersionUID = -1310401075732674852L;

    @Schema(name = "id", description = "教师ID")
    private UUID id;

    @Schema(name = "teacherCode", description = "教师工号")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    private String specialization;

    @Schema(name = "department", description = "所属部门/学院")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    private Integer education;

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
