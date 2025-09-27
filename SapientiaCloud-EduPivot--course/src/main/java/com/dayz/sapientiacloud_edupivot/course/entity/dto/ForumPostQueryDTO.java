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
@Schema(description = "论坛帖子查询数据传输对象")
public class ForumPostQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -5622883214353612807L;

    @Schema(name = "forumId", description = "所属论坛ID")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "发帖人ID")
    private UUID sysUserId;

    @Schema(name = "title", description = "帖子标题（模糊查询）")
    private String title;

    @Schema(name = "postType", description = "帖子类型 (0=普通帖子, 1=置顶帖子, 2=精华帖子, 3=公告)")
    private Integer postType;

    @Schema(name = "isAnonymous", description = "是否匿名发帖 (0=实名, 1=匿名)")
    private Integer isAnonymous;

    @Schema(name = "isTop", description = "是否置顶 (0=否, 1=是)")
    private Integer isTop;

    @Schema(name = "isEssence", description = "是否精华 (0=否, 1=是)")
    private Integer isEssence;

    @Schema(name = "isLocked", description = "是否锁定 (0=否, 1=是)")
    private Integer isLocked;

    @Schema(name = "status", description = "帖子状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    private Integer status;

    @Schema(name = "chapterId", description = "关联章节ID")
    private UUID chapterId;

    @Schema(name = "tag", description = "标签（模糊查询）")
    private String tag;
}
