package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程章节新增数据传输对象")
public class CourseChapterAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6058873313295897890L;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "teacherId", description = "创建教师ID")
    private UUID teacherId;

    @Schema(name = "chapterName", description = "章节名称")
    @NotBlank(message = "章节名称不能为空")
    @Size(max = 100, message = "章节名称不能超过100个字符")
    private String chapterName;

    @Schema(name = "parentChapterId", description = "父章节ID (用于构建章节树结构)")
    private UUID parentChapterId;

    @Schema(name = "description", description = "章节描述")
    @Size(max = 500, message = "章节描述不能超过500个字符")
    private String description;

    @Schema(name = "content", description = "章节内容 (富文本)")
    private String content;

    @Schema(name = "videoUrl", description = "视频资源URL")
    private String videoUrl;

    @Schema(name = "videoDuration", description = "视频时长(秒)")
    @Min(value = 0, message = "视频时长不能为负数")
    private Integer videoDuration;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "sortOrder", description = "排序权重")
    @Min(value = 0, message = "排序权重不能为负数")
    private Integer sortOrder;

    @Schema(name = "status", description = "章节状态 (0=草稿, 1=发布, 2=下架)")
    @Min(value = 0, message = "章节状态输入不正确")
    @Max(value = 2, message = "章节状态输入不正确")
    private Integer status;
}
