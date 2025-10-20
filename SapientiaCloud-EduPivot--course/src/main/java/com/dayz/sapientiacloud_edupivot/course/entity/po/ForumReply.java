package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_forum_reply")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "论坛回复持久化对象 (PO)")
public class ForumReply extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -3568363390833224545L;

    @Schema(name = "id", description = "回复ID")
    @Id
    private UUID id;

    @Schema(name = "postId", description = "所属帖子ID")
    @Field("post_id")
    private UUID postId;

    @Schema(name = "forumId", description = "所属论坛ID")
    @Field("forum_id")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "回复人ID")
    @Field("sys_user_id")
    private UUID sysUserId;

    @Schema(name = "content", description = "回复内容")
    @Field("content")
    private String content;

    @Schema(name = "parentReplyId", description = "父回复ID (用于构建回复树结构)")
    @Field("parent_reply_id")
    private UUID parentReplyId;

    @Schema(name = "replyToUserId", description = "回复目标用户ID")
    @Field("reply_to_user_id")
    private UUID replyToUserId;

    @Schema(name = "isAnonymous", description = "是否匿名回复 (0=实名, 1=匿名)")
    @Field("is_anonymous")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    @Field("attachment_urls")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    @Field("image_urls")
    private List<String> imageUrls;

    @Schema(name = "likeCount", description = "点赞次数")
    @Field("like_count")
    private Long likeCount;

    @Schema(name = "replyCount", description = "子回复次数")
    @Field("reply_count")
    private Long replyCount;

    @Schema(name = "isAccepted", description = "是否被采纳 (0=否, 1=是, 仅问答区有效)")
    @Field("is_accepted")
    private Integer isAccepted;

    @Schema(name = "floorNumber", description = "楼层号")
    @Field("floor_number")
    private Integer floorNumber;

    @Schema(name = "status", description = "回复状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    @Field("status")
    private Integer status;

    @Schema(name = "ipAddress", description = "发帖IP地址")
    @Field("ip_address")
    private String ipAddress;

    @Schema(name = "userAgent", description = "用户代理信息")
    @Field("user_agent")
    private String userAgent;
}
