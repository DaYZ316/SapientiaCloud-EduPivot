package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程论坛视图对象 (VO)")
public class CourseForumVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6525888944945547533L;

    @Schema(name = "id", description = "论坛ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "forumName", description = "论坛名称")
    private String forumName;

    @Schema(name = "description", description = "论坛描述")
    private String description;

    @Schema(name = "forumType", description = "论坛类型 (0=讨论区, 1=问答区, 2=作业区, 3=公告区)")
    private Integer forumType;

    @Schema(name = "isPublic", description = "是否公开 (0=仅课程成员, 1=公开)")
    private Integer isPublic;

    @Schema(name = "allowAnonymous", description = "是否允许匿名发帖 (0=不允许, 1=允许)")
    private Integer allowAnonymous;

    @Schema(name = "postCount", description = "帖子总数")
    private Long postCount;

    @Schema(name = "replyCount", description = "回复总数")
    private Long replyCount;

    @Schema(name = "status", description = "论坛状态 (0=正常, 1=关闭, 2=维护)")
    private Integer status;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
