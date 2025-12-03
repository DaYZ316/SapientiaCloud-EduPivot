package com.dayz.sapientiacloud_edupivot.live.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LiveRoomMessageDTO {

    @Schema(description = "消息内容")
    @NotBlank
    @Size(max = 1024)
    private String content;

    @Schema(description = "消息类型(text/system等)")
    private String messageType;

    @Schema(description = "发送人角色(0=学生,1=老师,2=助教)")
    private Integer senderRole;
}


