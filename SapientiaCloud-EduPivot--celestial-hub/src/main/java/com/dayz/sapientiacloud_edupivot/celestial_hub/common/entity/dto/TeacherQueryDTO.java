package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "教师分页查询DTO")
public class TeacherQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = 8722634881537900052L;

    @Schema(name = "teacherCode", description = "教师工号", example = "T001")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名", example = "李老师")
    private String realName;

    @Schema(name = "department", description = "所属部门/学院", example = "计算机学院")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)", example = "3")
    private Integer education;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)", example = "0")
    private Integer status;
}
