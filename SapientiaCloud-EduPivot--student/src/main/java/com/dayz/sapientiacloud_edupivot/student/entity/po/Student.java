package com.dayz.sapientiacloud_edupivot.student.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.student.common.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_student")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学生信息持久化对象 (PO)")
public class Student extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 436405259626355367L;

    @Schema(name = "id", description = "学生ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "studentCode", description = "学号")
    @TableField("student_code")
    private String studentCode;

    @Schema(name = "realName", description = "学生真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "admissionYear", description = "入学年份")
    @TableField("admission_year")
    private Integer admissionYear;

    @Schema(name = "major", description = "专业")
    @TableField("major")
    private String major;

    @Schema(name = "academicStatus", description = "学籍状态 (0=在读, 1=休学, 2=退学, 3=毕业)")
    @TableField("academic_status")
    private Integer academicStatus;

    @Schema(name = "description", description = "自我描述")
    @TableField("description")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    @TableField("sys_user_id")
    private UUID sysUserId;
}