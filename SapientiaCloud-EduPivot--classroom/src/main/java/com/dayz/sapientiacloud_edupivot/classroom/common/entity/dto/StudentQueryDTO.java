package com.dayz.sapientiacloud_edupivot.classroom.common.entity.dto;

import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学生分页查询DTO")
public class StudentQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = 8722634881537900052L;

    @Schema(name = "studentCode", description = "学生学号", example = "2021001")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名", example = "张三")
    private String realName;

    @Schema(name = "major", description = "专业", example = "计算机科学与技术")
    private String major;

    @Schema(name = "grade", description = "年级", example = "2021")
    private String grade;

    @Schema(name = "className", description = "班级", example = "计科2101")
    private String className;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)", example = "1")
    private Integer education;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)", example = "0")
    private Integer status;
}
