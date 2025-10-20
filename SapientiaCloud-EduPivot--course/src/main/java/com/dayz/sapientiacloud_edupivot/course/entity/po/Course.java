package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.config.UuidListTypeHandler;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程信息持久化对象 (PO)")
public class Course extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "课程ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "courseName", description = "课程名称")
    @TableField("course_name")
    private String courseName;

    @Schema(name = "teacherId", description = "授课教师ID")
    @TableField("teacher_id")
    private UUID teacherId;

    @Schema(name = "assistantTeacherIds", description = "辅助教学教师ID列表")
    @TableField(value = "assistant_teacher_ids", typeHandler = UuidListTypeHandler.class)
    private List<UUID> assistantTeacherIds;

    @Schema(name = "description", description = "课程描述")
    @TableField("description")
    private String description;

    @Schema(name = "coverImageUrl", description = "课程封面图片URL")
    @TableField("cover_image_url")
    private String coverImageUrl;

    @Schema(name = "semester", description = "开设学期")
    @TableField("semester")
    private String semester;

    @Schema(name = "location", description = "上课地点")
    @TableField("location")
    private String location;

    @Schema(name = "courseType", description = "课程类型 (0=必修, 1=选修)")
    @TableField("course_type")
    private Integer courseType;

    @Schema(name = "status", description = "课程状态 (0=正常, 1=停课)", example = "0")
    @TableField("status")
    private Integer status;
}
