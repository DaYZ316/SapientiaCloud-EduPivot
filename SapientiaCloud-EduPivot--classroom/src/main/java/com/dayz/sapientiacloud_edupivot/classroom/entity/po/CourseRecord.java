package com.dayz.sapientiacloud_edupivot.classroom.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_record")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教学记录持久化对象 (PO)")
public class CourseRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -989239423882115364L;

    @Schema(description = "课程记录ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(description = "关联课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(description = "授课教师ID")
    @TableField("teacher_id")
    private UUID teacherId;

    @Schema(description = "课程名称")
    @TableField("course_name")
    private String courseName;

    @Schema(description = "课程内容简介")
    @TableField("course_description")
    private String courseDescription;

    @Schema(description = "教室类型 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)")
    @TableField("classroom_type")
    private Integer classroomType;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    @TableField("layout_rows")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    @TableField("layout_columns")
    private Integer layoutColumns;

    @Schema(description = "课程开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    @TableField("over_time")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=已取消)")
    @TableField("status")
    private Integer status;
}
