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
@Schema(description = "课程学生详细信息视图对象 (DetailVO)")
public class CourseStudentDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "studentId", description = "学生ID")
    private UUID studentId;

    @Schema(name = "studentName", description = "学生姓名")
    private String studentName;

    @Schema(name = "studentNumber", description = "学号")
    private String studentNumber;

    @Schema(name = "courseId", description = "课程ID")
    private UUID courseId;

    @Schema(name = "courseName", description = "课程名称")
    private String courseName;

    @Schema(name = "grade", description = "成绩")
    private BigDecimal grade;

    @Schema(name = "enrollmentDate", description = "选课日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enrollmentDate;

    @Schema(name = "status", description = "选课状态 (0=在读, 1=已退课, 2=已完成)", example = "0")
    private Integer status;

    @Schema(name = "teacherId", description = "授课教师ID")
    private UUID teacherId;

    @Schema(name = "teacherName", description = "授课教师姓名")
    private String teacherName;

    @Schema(name = "semester", description = "学期")
    private String semester;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
