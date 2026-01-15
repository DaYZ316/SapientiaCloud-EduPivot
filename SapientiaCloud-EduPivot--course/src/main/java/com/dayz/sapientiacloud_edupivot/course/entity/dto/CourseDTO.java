package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程信息数据传输对象")
public class CourseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4459987964939141706L;

    @Schema(name = "id", description = "课程ID，更新时必须提供")
    private UUID id;

    @Schema(name = "courseName", description = "课程名称")
    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称不能超过100个字符")
    private String courseName;

    @Schema(name = "teacherId", description = "授课教师ID")
    @NotNull(message = "授课教师ID不能为空")
    private UUID teacherId;

    @Schema(name = "assistantTeacherIds", description = "辅助教学教师ID列表")
    private List<UUID> assistantTeacherIds;

    @Schema(name = "description", description = "课程描述")
    private String description;

    @Schema(name = "coverImageUrl", description = "课程封面图片URL")
    private String coverImageUrl;

    @Schema(name = "semester", description = "开设学期")
    @Size(max = 20, message = "学期不能超过20个字符")
    private String semester;

    @Schema(name = "location", description = "上课地点")
    @Size(max = 100, message = "上课地点不能超过100个字符")
    private String location;

    @Schema(name = "courseType", description = "课程类型 (0=必修, 1=选修)")
    @Min(value = 0, message = "课程类型输入不正确")
    @Max(value = 1, message = "课程类型输入不正确")
    private Integer courseType;

    @Schema(name = "status", description = "课程状态 (0=正常, 1=停课)")
    @Min(value = 0, message = "课程状态输入不正确")
    @Max(value = 1, message = "课程状态输入不正确")
    private Integer status;

    @Schema(name = "isPublic", description = "是否公开 (0=仅课程成员, 1=公开)")
    @Min(value = 0, message = "公开状态输入不正确")
    @Max(value = 1, message = "公开状态输入不正确")
    private Integer isPublic;
}
