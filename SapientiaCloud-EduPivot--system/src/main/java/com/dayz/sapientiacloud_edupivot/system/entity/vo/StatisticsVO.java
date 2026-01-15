package com.dayz.sapientiacloud_edupivot.system.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统计数据视图对象
 */
@Data
@Schema(description = "统计数据视图对象 (VO)")
public class StatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8879095778150237771L;

    @Schema(name = "studentCount", description = "学生总数")
    private Long studentCount;

    @Schema(name = "teacherCount", description = "教师总数")
    private Long teacherCount;

    @Schema(name = "courseCount", description = "课程总数")
    private Long courseCount;
}
