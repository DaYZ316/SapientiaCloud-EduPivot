package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课程学生视图对象 (VO)")
public class CourseStudentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "studentId", description = "学生ID")
    private UUID studentId;

    @Schema(name = "courseId", description = "课程ID")
    private UUID courseId;

    @Schema(name = "grade", description = "成绩")
    private BigDecimal grade;

    @Schema(name = "enrollmentDate", description = "选课日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollmentDate;

    @Schema(name = "status", description = "选课状态 (0=在读, 1=已退课, 2=已完成)", example = "0")
    private Integer status;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
