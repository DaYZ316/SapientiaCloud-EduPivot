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
@Schema(description = "教师信息视图对象 (VO)")
public class TeacherVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 8521685108180660843L;

    @Schema(name = "id", description = "教师ID")
    private UUID id;

    @Schema(name = "teacherCode", description = "教师工号")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "department", description = "所属部门/学院")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    private Integer education;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    private String specialization;

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