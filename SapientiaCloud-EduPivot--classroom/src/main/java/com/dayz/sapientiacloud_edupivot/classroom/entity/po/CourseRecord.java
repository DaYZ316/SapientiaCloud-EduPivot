package com.dayz.sapientiacloud_edupivot.classroom.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.classroom.common.config.JsonTypeHandler;
import com.dayz.sapientiacloud_edupivot.classroom.common.config.UuidListTypeHandler;
import com.dayz.sapientiacloud_edupivot.classroom.entity.bo.LayoutConfig;
import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_record")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教学记录持久化对象 (PO)")
public class CourseRecord extends BaseEntity  {


    @Serial
    private static final long serialVersionUID = -989239423882115364L;
    @Schema(description = "课程记录ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(description = "关联课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(description = "授课教师系统用户ID")
    @TableField("teacher_id")
    private UUID teacherId;

    @Schema(description = "参与学生ID列表 (JSON数组)")
    @TableField(value = "student_ids", typeHandler = UuidListTypeHandler.class)
    private List<UUID> studentIds;

    @Schema(description = "课堂互动题目ID列表 (JSON数组)")
    @TableField(value = "question_ids", typeHandler = UuidListTypeHandler.class)
    private List<UUID> questionIds;

    @Schema(description = "教室模型类型 (classroomSmall, classroomMiddle, classroomLarge)")
    @TableField("model_type")
    private String modelType;

    @Schema(description = "桌椅总数 (1-200)")
    @TableField("total_desks")
    private Integer totalDesks;

    @Schema(description = "行数 (仅传统布局或对齐布局使用)")
    @TableField("layout_rows")
    private Integer layoutRows;

    @Schema(description = "列数 (仅传统布局或对齐布局使用)")
    @TableField("layout_columns")
    private Integer layoutColumns;

    @Schema(description = "桌椅间距系数 (0.7-1.5)")
    @TableField("spacing")
    private Float spacing;

    @Schema(description = "布局详细参数")
    @TableField(value = "layout_config", typeHandler = JsonTypeHandler.class)
    private LayoutConfig layoutConfig;

    @Schema(description = "【废弃】旧版布局字段，仅兼容早期版本")
    @TableField(value = "classroom_layout", typeHandler = JsonTypeHandler.class)
    private Object classroomLayout;

    @Schema(description = "课程开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "课程结束时间")
    @TableField("over_time")
    private LocalDateTime overTime;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=取消)")
    @TableField("status")
    private Integer status;
}
