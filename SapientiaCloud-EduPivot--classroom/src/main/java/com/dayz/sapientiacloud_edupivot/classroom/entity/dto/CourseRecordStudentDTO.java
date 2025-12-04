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
    private static final long serialVersionUID = -8939844696227150345L;

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

    @Schema(description = "学生座位的x坐标")
    @NotNull(message = "学生座位的x坐标不能为空")
    private Integer locationX;

    @Schema(description = "学生座位的y坐标")
    @NotNull(message = "学生座位的y坐标不能为空")
    private Integer locationY;

    @Schema(description = "座位状态 (0=正常, 2=已预留, 3=已占用)")
    @Min(value = 0, message = "座位状态值无效")
    @Max(value = 3, message = "座位状态值无效")
    private Integer seatStatus;
}
