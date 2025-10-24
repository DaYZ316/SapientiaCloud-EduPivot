package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "课程教学学生参与数据传输对象")
public class CourseRecordStudentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课程记录ID")
    @NotNull(message = "课程记录ID不能为空")
    private UUID recordId;

    @Schema(description = "学生ID")
    @NotNull(message = "学生ID不能为空")
    private UUID studentId;

    @Schema(description = "课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(description = "座位编号 (从0开始)")
    @NotNull(message = "座位编号不能为空")
    @Min(value = 0, message = "座位编号不能小于0")
    private Integer seatIndex;

    @Schema(description = "3D坐标X (横向)")
    @NotNull(message = "3D坐标X不能为空")
    private Float locationX;

    @Schema(description = "3D坐标Y (高度)")
    @NotNull(message = "3D坐标Y不能为空")
    private Float locationY;

    @Schema(description = "3D坐标Z (纵深)")
    @NotNull(message = "3D坐标Z不能为空")
    private Float locationZ;

    @Schema(description = "朝向角度 (弧度制)")
    private Float rotationY;

    @Schema(description = "座位状态 (normal, marked, reserved, occupied)")
    @Size(max = 20, message = "座位状态不能超过20个字符")
    private String seatStatus;

    @Schema(description = "出勤状态 (0=未签到, 1=已签到, 2=缺席)")
    @Min(value = 0, message = "出勤状态值无效")
    @Max(value = 2, message = "出勤状态值无效")
    private Integer attendanceStatus;

    @Schema(description = "课堂互动得分 (可选)")
    @DecimalMin(value = "0.0", message = "互动得分不能小于0")
    @DecimalMax(value = "100.0", message = "互动得分不能大于100")
    private Float participationScore;
}
