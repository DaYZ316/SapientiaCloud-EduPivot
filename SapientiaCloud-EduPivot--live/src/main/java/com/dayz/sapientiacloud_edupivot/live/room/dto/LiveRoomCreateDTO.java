package com.dayz.sapientiacloud_edupivot.live.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class LiveRoomCreateDTO {
    @Schema(description = "房间名称")
    @NotBlank
    @Size(max = 128)
    private String roomName;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "课堂记录ID")
    private UUID classroomId;

    @Schema(description = "最大并发人数")
    private Integer maxParticipants;

    @Schema(description = "是否开启录制(0/1)")
    private Integer recordingEnabled;
}