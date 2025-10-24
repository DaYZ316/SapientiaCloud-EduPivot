package com.dayz.sapientiacloud_edupivot.classroom.entity.vo;

import com.dayz.sapientiacloud_edupivot.classroom.entity.bo.LayoutConfig;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程教学记录视图对象 (VO)")
public class CourseRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课程记录ID")
    private UUID id;

    @Schema(description = "关联课程ID")
    private UUID courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "授课教师系统用户ID")
    private UUID teacherId;

    @Schema(description = "授课教师姓名")
    private String teacherName;

    @Schema(description = "授课教师头像")
    private String teacherAvatar;

    @Schema(description = "参与学生ID列表 (JSON数组)")
    private List<UUID> studentIds;

    @Schema(description = "课堂互动题目ID列表 (JSON数组)")
    private List<UUID> questionIds;

    @Schema(description = "教室模型类型 (classroomSmall, classroomMiddle, classroomLarge)")
    private String modelType;

    @Schema(description = "桌椅总数 (1-200)")
    private Integer totalDesks;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    private Integer layoutColumns;

    @Schema(description = "桌椅间距系数 (0.7-1.5)")
    private Float spacing;

    @Schema(description = "布局详细参数")
    private LayoutConfig layoutConfig;

    @Schema(description = "【废弃】旧版布局字段，仅兼容早期版本")
    private Object classroomLayout;

    @Schema(description = "课程开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=取消)")
    private Integer status;

    @Schema(description = "应到人数")
    private Integer expectedStudents;

    @Schema(description = "实到人数")
    private Integer actualStudents;

    @Schema(description = "出勤率 (%)")
    private Double attendanceRate;

    @Schema(description = "课程时长 (分钟)")
    private Integer durationMinutes;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
