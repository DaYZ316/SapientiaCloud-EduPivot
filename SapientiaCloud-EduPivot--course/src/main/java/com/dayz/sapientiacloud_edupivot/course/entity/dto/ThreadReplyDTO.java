package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程论坛回复数据传输对象 (DTO)")
public class ThreadReplyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -297068011256997162L;

    @Schema(name = "id", description = "回复ID (更新时必填)")
    private UUID id;

    @Schema(name = "threadId", description = "所属主贴ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "主贴ID不能为空")
    private UUID threadId;

    @Schema(name = "userId", description = "回复用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private UUID userId;

    @Schema(name = "parentReplyId", description = "父回复ID (用于支持楼中楼回复)")
    private UUID parentReplyId;

    @Schema(name = "content", description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回复内容不能为空")
    private String content;
}
