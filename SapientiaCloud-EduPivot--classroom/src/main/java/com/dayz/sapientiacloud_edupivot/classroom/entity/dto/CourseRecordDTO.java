package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课程教学记录数据传输对象")
public class CourseRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1323839615699644389L;

    @Schema(description = "课程记录ID，更新时必须提供")
    private UUID id;

    @Schema(description = "关联课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(description = "授课教师ID")
    @NotNull(message = "教师ID不能为空")
    private UUID teacherId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程内容简介")
    private String courseDescription;

    @Schema(description = "教室类型 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)")
    @NotNull(message = "教室类型不能为空")
    @Min(value = 0, message = "教室类型值无效")
    @Max(value = 3, message = "教室类型值无效")
    private Integer classroomType;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    @Min(value = 1, message = "行数最小为1")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    @Min(value = 1, message = "列数最小为1")
    private Integer layoutColumns;

    @Schema(description = "课程开始时间")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=已取消)")
    @Min(value = 0, message = "课程状态值无效")
    @Max(value = 3, message = "课程状态值无效")
    private Integer status;
}
