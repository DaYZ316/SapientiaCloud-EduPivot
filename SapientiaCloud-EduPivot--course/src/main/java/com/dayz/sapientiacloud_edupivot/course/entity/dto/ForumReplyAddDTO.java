package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "论坛回复新增数据传输对象")
public class ForumReplyAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3568363390833224545L;

    @Schema(name = "postId", description = "所属帖子ID")
    @NotNull(message = "帖子ID不能为空")
    private UUID postId;

    @Schema(name = "forumId", description = "所属论坛ID")
    @NotNull(message = "论坛ID不能为空")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "回复人ID")
    @NotNull(message = "回复人ID不能为空")
    private UUID sysUserId;

    @Schema(name = "content", description = "回复内容")
    @NotBlank(message = "回复内容不能为空")
    private String content;

    @Schema(name = "parentReplyId", description = "父回复ID (用于构建回复树结构)")
    private UUID parentReplyId;

    @Schema(name = "replyToUserId", description = "回复目标用户ID")
    private UUID replyToUserId;

    @Schema(name = "isAnonymous", description = "是否匿名回复 (0=实名, 1=匿名)")
    @Min(value = 0, message = "匿名回复标识输入不正确")
    @Max(value = 1, message = "匿名回复标识输入不正确")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;
}
