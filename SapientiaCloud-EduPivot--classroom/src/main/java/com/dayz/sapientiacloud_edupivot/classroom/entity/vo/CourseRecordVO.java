package com.dayz.sapientiacloud_edupivot.classroom.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课程教学记录视图对象 (VO)")
public class CourseRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5787126666907488112L;

    @Schema(description = "课程记录ID")
    private UUID id;

    @Schema(description = "关联课程ID")
    private UUID courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程内容简介")
    private String courseDescription;

    @Schema(description = "授课教师ID")
    private UUID teacherId;

    @Schema(description = "授课教师姓名")
    private String teacherName;

    @Schema(description = "授课教师头像")
    private String teacherAvatar;

    @Schema(description = "教室类型 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)")
    private Integer classroomType;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    private Integer layoutColumns;

    @Schema(description = "课程开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=课前准备, 1=上课中, 2=下课，此字段由系统根据时间自动计算)")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
