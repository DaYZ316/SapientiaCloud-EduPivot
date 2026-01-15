package com.dayz.sapientiacloud_edupivot.live.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "直播房间令牌请求数据传输对象")
public class LiveRoomTokenRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户角色(0=学生,1=老师,2=助教)")
    @NotNull(message = "用户角色不能为空")
    private Integer role;
}