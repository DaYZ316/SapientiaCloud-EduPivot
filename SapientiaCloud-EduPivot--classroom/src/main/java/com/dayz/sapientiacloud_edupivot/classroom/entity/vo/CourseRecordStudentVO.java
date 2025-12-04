package com.dayz.sapientiacloud_edupivot.classroom.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课程教学学生参与视图对象 (VO)")
public class CourseRecordStudentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1109725723873263506L;

    @Schema(description = "课程记录ID")
    private UUID recordId;

    @Schema(description = "学生ID")
    private UUID studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentCode;

    @Schema(description = "学生头像")
    private String studentAvatar;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "座位编号 (从0开始)")
    private Integer seatIndex;

    @Schema(description = "学生座位的x坐标")
    private Integer locationX;

    @Schema(description = "学生座位的y坐标")
    private Integer locationY;

    @Schema(description = "座位状态 (0=正常, 2=已预留, 3=已占用)")
    private Integer seatStatus;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
