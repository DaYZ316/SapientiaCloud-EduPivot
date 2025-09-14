package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_course_thread")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程论坛主贴持久化对象 (PO)")
public class CourseThread extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "主贴ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @TableField("course_id")
    private UUID courseId;

    @Schema(name = "userId", description = "发帖用户ID")
    @TableField("user_id")
    private UUID userId;

    @Schema(name = "title", description = "帖子标题")
    @TableField("title")
    private String title;

    @Schema(name = "content", description = "帖子内容 (使用longtext以支持富文本和长内容)")
    @TableField("content")
    private String content;

    @Schema(name = "isPinned", description = "是否置顶 (1=是, 0=否)")
    @TableField("is_pinned")
    private Integer pinned;

    @Schema(name = "isClosed", description = "是否关闭/锁定 (1=是, 0=否, 关闭后无法回复)")
    @TableField("is_closed")
    private Integer closed;

    @Schema(name = "viewCount", description = "浏览次数")
    @TableField("view_count")
    private Integer viewCount;

    @Schema(name = "replyCount", description = "回复总数 (冗余字段, 提高查询性能)")
    @TableField("reply_count")
    private Integer replyCount;

    @Schema(name = "lastReplyTime", description = "最后回复时间 (冗余字段, 用于排序)")
    @TableField("last_reply_time")
    private LocalDateTime lastReplyTime;
}
