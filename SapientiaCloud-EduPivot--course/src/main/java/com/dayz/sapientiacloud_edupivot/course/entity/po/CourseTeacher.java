package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_teacher")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教师团队关联信息持久化对象 (PO)")
public class CourseTeacher extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1593084056949994885L;

    @Schema(name = "courseId", description = "课程ID")
    @TableId(value = "course_id")
    private UUID courseId;

    @Schema(name = "teacherId", description = "教师ID")
    @TableId(value = "teacher_id")
    private UUID teacherId;

    @Schema(name = "roleType", description = "角色类型 (0=负责人, 1=教学团队成员)")
    @TableField("role_type")
    private Integer roleType;
}
