package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程论坛新增数据传输对象")
public class CourseForumAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5454792117895824447L;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "forumName", description = "论坛名称")
    @NotBlank(message = "论坛名称不能为空")
    @Size(max = 100, message = "论坛名称不能超过100个字符")
    private String forumName;

    @Schema(name = "description", description = "论坛描述")
    @Size(max = 500, message = "论坛描述不能超过500个字符")
    private String description;

    @Schema(name = "forumType", description = "论坛类型 (0=讨论区, 1=问答区, 2=作业区, 3=公告区)")
    @NotNull(message = "论坛类型不能为空")
    @Min(value = 0, message = "论坛类型输入不正确")
    @Max(value = 3, message = "论坛类型输入不正确")
    private Integer forumType;

    @Schema(name = "isPublic", description = "是否公开 (0=仅课程成员, 1=公开)")
    @NotNull(message = "公开标识不能为空")
    @Min(value = 0, message = "公开标识输入不正确")
    @Max(value = 1, message = "公开标识输入不正确")
    private Integer isPublic;

    @Schema(name = "allowAnonymous", description = "是否允许匿名发帖 (0=不允许, 1=允许)")
    @NotNull(message = "匿名发帖标识不能为空")
    @Min(value = 0, message = "匿名发帖标识输入不正确")
    @Max(value = 1, message = "匿名发帖标识输入不正确")
    private Integer allowAnonymous;

    @Schema(name = "status", description = "论坛状态 (0=正常, 1=关闭, 2=维护)")
    @Min(value = 0, message = "论坛状态输入不正确")
    @Max(value = 2, message = "论坛状态输入不正确")
    private Integer status;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;
}
