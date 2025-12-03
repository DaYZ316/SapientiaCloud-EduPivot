package com.dayz.sapientiacloud_edupivot.live.room.entity.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "live_room_message")
@Schema(description = "直播房间聊天消息文档")
public class LiveRoomMessage {

    @Id
    @Schema(description = "消息ID")
    private UUID id;

    @Schema(description = "直播房间ID")
    private UUID liveRoomId;

    @Schema(description = "发送人ID")
    private UUID senderId;

    @Schema(description = "发送人名称")
    private String senderName;

    @Schema(description = "发送人角色(0=学生,1=老师,2=助教)")
    private Integer senderRole;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型(text/system等)")
    private String messageType;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
}


