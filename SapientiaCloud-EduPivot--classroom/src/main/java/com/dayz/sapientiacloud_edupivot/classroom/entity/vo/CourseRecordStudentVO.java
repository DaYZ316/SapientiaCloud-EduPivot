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
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "座位编号 (从0开始)")
    private Integer seatIndex;

    @Schema(description = "3D坐标X (横向)")
    private Float locationX;

    @Schema(description = "3D坐标Y (高度)")
    private Float locationY;

    @Schema(description = "3D坐标Z (纵深)")
    private Float locationZ;

    @Schema(description = "朝向角度 (弧度制)")
    private Float rotationY;

    @Schema(description = "座位状态 (normal, marked, reserved, occupied)")
    private String seatStatus;

    @Schema(description = "出勤状态 (0=未签到, 1=已签到, 2=缺席)")
    private Integer attendanceStatus;

    @Schema(description = "课堂互动得分 (可选)")
    private Float participationScore;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
