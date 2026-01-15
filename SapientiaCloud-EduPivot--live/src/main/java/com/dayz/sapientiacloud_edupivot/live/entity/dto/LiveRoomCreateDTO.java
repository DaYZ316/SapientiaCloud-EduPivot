package com.dayz.sapientiacloud_edupivot.live.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "创建直播房间数据传输对象")
public class LiveRoomCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "房间名称")
    @NotBlank(message = "房间名称不能为空")
    @Size(max = 128, message = "房间名称不能超过128个字符")
    private String roomName;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "课堂记录ID（mg_course_record.id）")
    @NotNull(message = "课堂记录ID不能为空")
    private UUID classroomId;

    @Schema(description = "最大并发人数")
    private Integer maxParticipants;

    @Schema(description = "是否开启录制(0/1)")
    private Integer recordingEnabled;
}