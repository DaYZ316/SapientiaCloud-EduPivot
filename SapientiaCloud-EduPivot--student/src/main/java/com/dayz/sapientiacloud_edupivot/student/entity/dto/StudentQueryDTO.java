package com.dayz.sapientiacloud_edupivot.student.entity.dto;

import com.dayz.sapientiacloud_edupivot.student.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学生信息查询条件")
public class StudentQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = -8335717222740700845L;

    @Schema(name = "studentCode", description = "学号")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名")
    private String realName;

    @Schema(name = "admissionYear", description = "入学年份")
    private Integer admissionYear;

    @Schema(name = "major", description = "专业")
    private String major;

    @Schema(name = "academicStatus", description = "学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)")
    private Integer academicStatus;

    @Schema(name = "status", description = "账号状态 (0=正常, 1=停用)")
    private Integer status;
}