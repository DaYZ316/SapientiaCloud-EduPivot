package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "论坛帖子视图对象 (VO)")
public class ForumPostVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6299236008168138253L;

    @Schema(name = "id", description = "帖子ID")
    private UUID id;

    @Schema(name = "forumId", description = "所属论坛ID")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "发帖人ID")
    private UUID sysUserId;


    @Schema(name = "title", description = "帖子标题")
    private String title;

    @Schema(name = "content", description = "帖子内容")
    private String content;

    @Schema(name = "postType", description = "帖子类型 (0=普通帖子, 1=置顶帖子, 2=精华帖子, 3=公告)")
    private Integer postType;

    @Schema(name = "isAnonymous", description = "是否匿名发帖 (0=实名, 1=匿名)")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "viewCount", description = "浏览次数")
    private Long viewCount;

    @Schema(name = "likeCount", description = "点赞次数")
    private Long likeCount;

    @Schema(name = "replyCount", description = "回复次数")
    private Long replyCount;

    @Schema(name = "shareCount", description = "分享次数")
    private Long shareCount;

    @Schema(name = "isTop", description = "是否置顶 (0=否, 1=是)")
    private Integer isTop;

    @Schema(name = "isEssence", description = "是否精华 (0=否, 1=是)")
    private Integer isEssence;

    @Schema(name = "isLocked", description = "是否锁定 (0=否, 1=是)")
    private Integer isLocked;

    @Schema(name = "lastReplyId", description = "最新回复ID")
    private UUID lastReplyId;

    @Schema(name = "lastReplyTime", description = "最新回复时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReplyTime;

    @Schema(name = "lastReplyUserId", description = "最新回复用户ID")
    private UUID lastReplyUserId;

    @Schema(name = "status", description = "帖子状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    private Integer status;

    @Schema(name = "chapterId", description = "关联章节ID")
    private UUID chapterId;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
