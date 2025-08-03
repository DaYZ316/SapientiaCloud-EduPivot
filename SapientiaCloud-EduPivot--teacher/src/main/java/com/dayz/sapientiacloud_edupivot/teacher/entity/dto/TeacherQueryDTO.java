package com.dayz.sapientiacloud_edupivot.teacher.entity.dto;

import com.dayz.sapientiacloud_edupivot.teacher.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "教师信息查询条件")
public class TeacherQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = 875100828032488629L;

    @Schema(name = "teacherCode", description = "教师工号")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名")
    private String realName;

    @Schema(name = "department", description = "所属部门/学院")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    private Integer education;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    private String specialization;

    @Schema(name = "status", description = "账号状态 (0=正常, 1=停用)")
    private Integer status;
}