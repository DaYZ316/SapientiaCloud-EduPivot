package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_student")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程学生关联持久化对象 (PO)")
public class CourseStudent extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "studentId", description = "学生ID")
    @TableField(value = "student_id")
    private UUID studentId;

    @Schema(name = "courseId", description = "课程ID")
    @TableField(value = "course_id")
    private UUID courseId;

    @Schema(name = "grade", description = "成绩")
    @TableField("grade")
    private BigDecimal grade;

    @Schema(name = "enrollmentDate", description = "选课日期")
    @TableField("enrollment_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollmentDate;

    @Schema(name = "status", description = "选课状态 (0=在读, 1=已退课, 2=已完成)", example = "0")
    @TableField("status")
    private Integer status;
}
