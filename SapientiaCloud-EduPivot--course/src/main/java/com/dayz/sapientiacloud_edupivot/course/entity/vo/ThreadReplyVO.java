package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程论坛回复视图对象 (VO)")
public class ThreadReplyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "回复ID")
    private UUID id;

    @Schema(name = "threadId", description = "所属主贴ID")
    private UUID threadId;

    @Schema(name = "userId", description = "回复用户ID")
    private UUID userId;

    @Schema(name = "userName", description = "回复用户名称")
    private String userName;

    @Schema(name = "userAvatar", description = "回复用户头像")
    private String userAvatar;

    @Schema(name = "parentReplyId", description = "父回复ID")
    private UUID parentReplyId;

    @Schema(name = "content", description = "回复内容")
    private String content;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "children", description = "子回复列表")
    private List<ThreadReplyVO> children;
}
