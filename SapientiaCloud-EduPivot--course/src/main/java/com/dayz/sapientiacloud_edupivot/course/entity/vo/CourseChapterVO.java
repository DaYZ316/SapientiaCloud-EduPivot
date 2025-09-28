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
@Schema(description = "课程章节视图对象 (VO)")
public class CourseChapterVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1074576794464861220L;

    @Schema(name = "id", description = "章节ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "teacherId", description = "创建教师ID")
    private UUID teacherId;


    @Schema(name = "chapterName", description = "章节名称")
    private String chapterName;


    @Schema(name = "parentChapterId", description = "父章节ID")
    private UUID parentChapterId;

    @Schema(name = "description", description = "章节描述")
    private String description;

    @Schema(name = "content", description = "章节内容")
    private String content;

    @Schema(name = "videoUrl", description = "视频资源URL")
    private String videoUrl;

    @Schema(name = "videoDuration", description = "视频时长(秒)")
    private Integer videoDuration;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "sortOrder", description = "排序权重")
    private Integer sortOrder;

    @Schema(name = "status", description = "章节状态 (0=草稿, 1=发布, 2=下架)")
    private Integer status;

    @Schema(name = "viewCount", description = "浏览次数")
    private Long viewCount;

    @Schema(name = "likeCount", description = "点赞次数")
    private Long likeCount;

    @Schema(name = "commentCount", description = "评论次数")
    private Long commentCount;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "children", description = "子章节列表（用于树形结构）")
    private List<CourseChapterVO> children;
}
