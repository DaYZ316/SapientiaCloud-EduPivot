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
@Document(collection = "course_chapters")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程章节持久化对象 (PO)")
public class CourseChapter extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6058873313295897890L;

    @Schema(name = "id", description = "章节ID")
    @Id
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "teacherId", description = "创建教师ID")
    @Field("teacher_id")
    private UUID teacherId;

    @Schema(name = "chapterName", description = "章节名称")
    @Field("chapter_name")
    private String chapterName;


    @Schema(name = "parentChapterId", description = "父章节ID (用于构建章节树结构)")
    @Field("parent_chapter_id")
    private UUID parentChapterId;

    @Schema(name = "description", description = "章节描述")
    @Field("description")
    private String description;

    @Schema(name = "content", description = "章节内容 (富文本)")
    @Field("content")
    private String content;

    @Schema(name = "videoUrl", description = "视频资源URL")
    @Field("video_url")
    private String videoUrl;

    @Schema(name = "videoDuration", description = "视频时长(秒)")
    @Field("video_duration")
    private Integer videoDuration;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    @Field("attachment_urls")
    private List<String> attachmentUrls;

    @Schema(name = "sortOrder", description = "排序权重")
    @Field("sort_order")
    private Integer sortOrder;

    @Schema(name = "status", description = "章节状态 (0=草稿, 1=发布, 2=下架)")
    @Field("status")
    private Integer status;

    @Schema(name = "viewCount", description = "浏览次数")
    @Field("view_count")
    private Long viewCount;

    @Schema(name = "likeCount", description = "点赞次数")
    @Field("like_count")
    private Long likeCount;

    @Schema(name = "commentCount", description = "评论次数")
    @Field("comment_count")
    private Long commentCount;
}
