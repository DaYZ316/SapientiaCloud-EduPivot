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
@Schema(description = "论坛回复查询数据传输对象")
public class ForumReplyQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -1343668552154279771L;

    @Schema(name = "postId", description = "所属帖子ID")
    private UUID postId;

    @Schema(name = "forumId", description = "所属论坛ID")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "回复人ID")
    private UUID sysUserId;

    @Schema(name = "parentReplyId", description = "父回复ID")
    private UUID parentReplyId;

    @Schema(name = "replyToUserId", description = "回复目标用户ID")
    private UUID replyToUserId;

    @Schema(name = "isAnonymous", description = "是否匿名回复 (0=实名, 1=匿名)")
    private Integer isAnonymous;

    @Schema(name = "isAccepted", description = "是否被采纳 (0=否, 1=是)")
    private Integer isAccepted;

    @Schema(name = "status", description = "回复状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    private Integer status;
}
