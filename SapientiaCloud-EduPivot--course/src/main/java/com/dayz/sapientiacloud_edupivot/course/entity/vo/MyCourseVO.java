package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "我的课程视图对象 (VO)")
public class MyCourseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 622198178525254562L;

    @Schema(description = "课程VO")
    private CourseVO courseVO;

    @Schema(name = "grade", description = "成绩")
    private BigDecimal grade;

    @Schema(name = "status", description = "选课状态 (0=在读, 1=已退课, 2=已完成)", example = "0")
    private Integer status;

    @Schema(name = "createTime", description = "入课时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
