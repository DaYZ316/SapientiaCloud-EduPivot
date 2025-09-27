package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "论坛帖子数据传输对象")
public class ForumPostDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5017676351834399371L;

    @Schema(name = "id", description = "帖子ID，更新时必须提供")
    private UUID id;

    @Schema(name = "forumId", description = "所属论坛ID")
    @NotNull(message = "论坛ID不能为空")
    private UUID forumId;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "发帖人ID")
    @NotNull(message = "发帖人ID不能为空")
    private UUID sysUserId;

    @Schema(name = "title", description = "帖子标题")
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 200, message = "帖子标题不能超过200个字符")
    private String title;

    @Schema(name = "content", description = "帖子内容")
    @NotBlank(message = "帖子内容不能为空")
    private String content;

    @Schema(name = "postType", description = "帖子类型 (0=普通帖子, 1=置顶帖子, 2=精华帖子, 3=公告)")
    @Min(value = 0, message = "帖子类型输入不正确")
    @Max(value = 3, message = "帖子类型输入不正确")
    private Integer postType;

    @Schema(name = "isAnonymous", description = "是否匿名发帖 (0=实名, 1=匿名)")
    @Min(value = 0, message = "匿名发帖标识输入不正确")
    @Max(value = 1, message = "匿名发帖标识输入不正确")
    private Integer isAnonymous;

    @Schema(name = "attachmentUrls", description = "附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "isTop", description = "是否置顶 (0=否, 1=是)")
    @Min(value = 0, message = "置顶标识输入不正确")
    @Max(value = 1, message = "置顶标识输入不正确")
    private Integer isTop;

    @Schema(name = "isEssence", description = "是否精华 (0=否, 1=是)")
    @Min(value = 0, message = "精华标识输入不正确")
    @Max(value = 1, message = "精华标识输入不正确")
    private Integer isEssence;

    @Schema(name = "isLocked", description = "是否锁定 (0=否, 1=是)")
    @Min(value = 0, message = "锁定标识输入不正确")
    @Max(value = 1, message = "锁定标识输入不正确")
    private Integer isLocked;

    @Schema(name = "status", description = "帖子状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)")
    @Min(value = 0, message = "帖子状态输入不正确")
    @Max(value = 3, message = "帖子状态输入不正确")
    private Integer status;

    @Schema(name = "chapterId", description = "关联章节ID")
    private UUID chapterId;
}
