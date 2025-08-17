package com.dayz.sapientiacloud_edupivot.teacher.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.teacher.common.entity.base.BaseEntity;
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
@TableName("mg_teacher")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "教师信息持久化对象 (PO)")
public class Teacher extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7397963210170024952L;

    @Schema(name = "id", description = "教师ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "teacherCode", description = "教师工号")
    @TableField("teacher_code")
    private String teacherCode;

    @Schema(name = "realName", description = "教师真实姓名")
    @TableField("real_name")
    private String realName;

    @Schema(name = "birthDate", description = "出生日期")
    @TableField("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Schema(name = "department", description = "所属部门/学院")
    @TableField("department")
    private String department;

    @Schema(name = "education", description = "学历 (0=专科, 1=本科, 2=硕士, 3=博士)")
    @TableField("education")
    private Integer education;

    @Schema(name = "specialization", description = "专业特长/研究方向")
    @TableField("specialization")
    private String specialization;

    @Schema(name = "description", description = "自我描述")
    @TableField("description")
    private String description;

    @Schema(name = "sysUserId", description = "系统用户ID")
    @TableField("sys_user_id")
    private UUID sysUserId;
}