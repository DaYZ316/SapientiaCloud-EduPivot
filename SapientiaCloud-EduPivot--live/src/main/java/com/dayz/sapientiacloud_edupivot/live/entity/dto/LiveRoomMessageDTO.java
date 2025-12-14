package com.dayz.sapientiacloud_edupivot.live.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "直播房间消息数据传输对象")
public class LiveRoomMessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息内容")
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1024, message = "消息内容不能超过1024个字符")
    private String content;

    @Schema(description = "消息类型(text/system等)")
    private String messageType;

    @Schema(description = "发送人角色(0=学生,1=老师,2=助教)")
    private Integer senderRole;
}


