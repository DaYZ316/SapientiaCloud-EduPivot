package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import com.dayz.sapientiacloud_edupivot.classroom.entity.bo.LayoutConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程教学记录数据传输对象")
public class CourseRecordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课程记录ID，更新时必须提供")
    private UUID id;

    @Schema(description = "关联课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(description = "授课教师系统用户ID")
    @NotNull(message = "教师ID不能为空")
    private UUID teacherId;

    @Schema(description = "参与学生ID列表 (JSON数组)")
    private List<UUID> studentIds;

    @Schema(description = "课堂互动题目ID列表 (JSON数组)")
    private List<UUID> questionIds;

    @Schema(description = "教室模型类型 (classroomSmall, classroomMiddle, classroomLarge)")
    @NotBlank(message = "教室模型类型不能为空")
    @Size(max = 50, message = "教室模型类型不能超过50个字符")
    private String modelType;

    @Schema(description = "桌椅总数 (1-200)")
    @NotNull(message = "桌椅总数不能为空")
    @Min(value = 1, message = "桌椅总数最小为1")
    @Max(value = 200, message = "桌椅总数最大为200")
    private Integer totalDesks;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    @Min(value = 1, message = "行数最小为1")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    @Min(value = 1, message = "列数最小为1")
    private Integer layoutColumns;

    @Schema(description = "桌椅间距系数 (0.7-1.5)")
    @DecimalMin(value = "0.7", message = "间距系数最小为0.7")
    @DecimalMax(value = "1.5", message = "间距系数最大为1.5")
    private Float spacing;

    @Schema(description = "布局详细参数")
    private LayoutConfig layoutConfig;

    @Schema(description = "【废弃】旧版布局字段，仅兼容早期版本")
    private Object classroomLayout;

    @Schema(description = "课程开始时间")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=取消)")
    @Min(value = 0, message = "课程状态值无效")
    @Max(value = 3, message = "课程状态值无效")
    private Integer status;
}
