package com.dayz.sapientiacloud_edupivot.live.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "直播会话心跳/退出请求数据传输对象")
public class LiveRoomSessionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "直播房间ID")
    @NotNull(message = "直播房间ID不能为空")
    private UUID roomId;

    @Schema(description = "直播会话ID")
    @NotNull(message = "直播会话ID不能为空")
    private String sessionId;
}
