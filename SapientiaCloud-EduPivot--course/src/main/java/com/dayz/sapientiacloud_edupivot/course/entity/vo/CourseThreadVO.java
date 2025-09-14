package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程论坛主贴视图对象 (VO)")
public class CourseThreadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "主贴ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "userId", description = "发帖用户ID")
    private UUID userId;

    @Schema(name = "userName", description = "发帖用户名称")
    private String userName;

    @Schema(name = "userAvatar", description = "发帖用户头像")
    private String userAvatar;

    @Schema(name = "title", description = "帖子标题")
    private String title;

    @Schema(name = "content", description = "帖子内容")
    private String content;

    @Schema(name = "pinned", description = "是否置顶")
    private Integer pinned;

    @Schema(name = "closed", description = "是否关闭/锁定")
    private Integer closed;

    @Schema(name = "viewCount", description = "浏览次数")
    private Integer viewCount;

    @Schema(name = "replyCount", description = "回复总数")
    private Integer replyCount;

    @Schema(name = "lastReplyTime", description = "最后回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
