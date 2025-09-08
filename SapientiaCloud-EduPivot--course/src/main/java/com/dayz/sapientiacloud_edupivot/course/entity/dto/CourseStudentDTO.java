package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Schema(description = "课程学生数据传输对象")
public class CourseStudentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "studentId", description = "学生ID")
    @NotNull(message = "学生ID不能为空")
    private UUID studentId;

    @Schema(name = "courseId", description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "grade", description = "成绩")
    @Min(value = 0, message = "成绩不能小于0")
    @Max(value = 100, message = "成绩不能大于100")
    private BigDecimal grade;

    @Schema(name = "enrollmentDate", description = "选课日期")
    private LocalDate enrollmentDate;

    @Schema(name = "status", description = "选课状态 (0=在读, 1=已退课, 2=已完成)")
    @Min(value = 0, message = "选课状态输入不正确")
    @Max(value = 2, message = "选课状态输入不正确")
    private Integer status;
}
