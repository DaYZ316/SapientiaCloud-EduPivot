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
@Document(collection = "forum_posts")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "论坛帖子持久化对象 (PO)")
public class ForumPost extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1708331606402034683L;

    @Schema(name = "id", description = "帖子ID")
    @Id
    private UUID id;

    @Schema(name = "forumId", description = "所属论坛ID")
    @Field("forum_id")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "发帖人ID")
    @Field("sys_user_id")
    private UUID sysUserId;


    @Schema(name = "title", description = "帖子标题")
    @Field("title")
    private String title;

    @Schema(name = "content", description = "帖子内容")
    @Field("content")
    private String content;

    @Schema(name = "postType", description = "帖子类型 (0=普通帖子, 1=置顶帖子, 2=精华帖子, 3=公告)")
    @Field("post_type")
    private Integer postType;

    @Schema(name = "isAnonymous", description = "是否匿名发帖 (0=实名, 1=匿名)")
    @Field("is_anonymous")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    @Field("attachment_urls")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    @Field("image_urls")
    private List<String> imageUrls;

    @Schema(name = "tags", description = "标签列表")
    @Field("tags")
    private List<String> tags;

    @Schema(name = "viewCount", description = "浏览次数")
    @Field("view_count")
    private Long viewCount;

    @Schema(name = "likeCount", description = "点赞次数")
    @Field("like_count")
    private Long likeCount;

    @Schema(name = "replyCount", description = "回复次数")
    @Field("reply_count")
    private Long replyCount;

    @Schema(name = "shareCount", description = "分享次数")
    @Field("share_count")
    private Long shareCount;

    @Schema(name = "isTop", description = "是否置顶 (0=否, 1=是)")
    @Field("is_top")
    private Integer isTop;

    @Schema(name = "isEssence", description = "是否精华 (0=否, 1=是)")
    @Field("is_essence")
    private Integer isEssence;

    @Schema(name = "isLocked", description = "是否锁定 (0=否, 1=是)")
    @Field("is_locked")
    private Integer isLocked;

    @Schema(name = "lastReplyId", description = "最新回复ID")
    @Field("last_reply_id")
    private UUID lastReplyId;

    @Schema(name = "lastReplyTime", description = "最新回复时间")
    @Field("last_reply_time")
    private String lastReplyTime;

    @Schema(name = "lastReplyUserId", description = "最新回复用户ID")
    @Field("last_reply_user_id")
    private UUID lastReplyUserId;

    @Schema(name = "status", description = "帖子状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    @Field("status")
    private Integer status;

    @Schema(name = "chapterId", description = "关联章节ID (可选)")
    @Field("chapter_id")
    private UUID chapterId;
}
