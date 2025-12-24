package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_assistant_teacher")
@Schema(description = "课程-辅助教师关联持久化对象 (PO)(包括主讲教师)")
public class CourseAssistantTeacher implements Serializable {

    @Serial
    private static final long serialVersionUID = 9214809766267716674L;

    @Schema(name = "courseId", description = "课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(name = "assistantTeacherId", description = "辅助教师ID")
    @TableField("assistant_teacher_id")
    private UUID assistantTeacherId;

    @Schema(name = "createTime", description = "创建时间 (系统自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}


