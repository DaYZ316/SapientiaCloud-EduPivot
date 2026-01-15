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
@Schema(description = "论坛回复视图对象 (VO)")
public class ForumReplyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2956592387974980307L;

    @Schema(name = "id", description = "回复ID")
    private UUID id;

    @Schema(name = "postId", description = "所属帖子ID")
    private UUID postId;

    @Schema(name = "forumId", description = "所属论坛ID")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "回复人ID")
    private UUID sysUserId;

    @Schema(name = "userName", description = "回复人用户名")
    private String userName;

    @Schema(name = "userAvatar", description = "回复人头像URL")
    private String userAvatar;

    @Schema(name = "content", description = "回复内容")
    private String content;

    @Schema(name = "parentReplyId", description = "父回复ID")
    private UUID parentReplyId;

    @Schema(name = "replyToUserId", description = "回复目标用户ID")
    private UUID replyToUserId;

    @Schema(name = "replyToUserName", description = "回复目标用户昵称")
    private String replyToUserName;

    @Schema(name = "isAnonymous", description = "是否匿名回复 (0=实名, 1=匿名)")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "likeCount", description = "点赞次数")
    private Long likeCount;

    @Schema(name = "replyCount", description = "子回复次数")
    private Long replyCount;

    @Schema(name = "isAccepted", description = "是否被采纳 (0=否, 1=是)")
    private Integer isAccepted;

    @Schema(name = "floorNumber", description = "楼层号")
    private Integer floorNumber;

    @Schema(name = "status", description = "回复状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    private Integer status;

    @Schema(name = "ipAddress", description = "发帖IP地址")
    private String ipAddress;

    @Schema(name = "userAgent", description = "用户代理信息")
    private String userAgent;

    @Schema(name = "children", description = "子回复列表")
    private List<ForumReplyVO> children;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "deleted", description = "是否删除 (0=未删除, 1=已删除)")
    private Integer deleted;
}
