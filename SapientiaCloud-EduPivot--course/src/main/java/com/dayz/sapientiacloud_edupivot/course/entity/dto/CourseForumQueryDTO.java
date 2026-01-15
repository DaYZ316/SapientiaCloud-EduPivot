package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程论坛查询数据传输对象")
public class CourseForumQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -2698503478236949528L;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "forumName", description = "论坛名称（模糊查询）")
    private String forumName;

    @Schema(name = "forumType", description = "论坛类型 (0=讨论区, 1=问答区, 2=作业区, 3=公告区)")
    private Integer forumType;

    @Schema(name = "allowAnonymous", description = "是否允许匿名发帖 (0=不允许, 1=允许)")
    private Integer allowAnonymous;

    @Schema(name = "status", description = "论坛状态 (0=正常, 1=关闭, 2=维护)")
    private Integer status;

    @Schema(name = "moderatorId", description = "版主ID")
    private UUID moderatorId;
}
