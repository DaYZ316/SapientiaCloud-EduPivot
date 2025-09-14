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
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("mg_thread_reply")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程论坛回复持久化对象 (PO)")
public class ThreadReply extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "回复ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "threadId", description = "所属主贴ID")
    @TableField("thread_id")
    private UUID threadId;

    @Schema(name = "userId", description = "回复用户ID")
    @TableField("user_id")
    private UUID userId;

    @Schema(name = "parentReplyId", description = "父回复ID (用于支持楼中楼回复)")
    @TableField("parent_reply_id")
    private UUID parentReplyId;

    @Schema(name = "content", description = "回复内容")
    @TableField("content")
    private String content;
}
