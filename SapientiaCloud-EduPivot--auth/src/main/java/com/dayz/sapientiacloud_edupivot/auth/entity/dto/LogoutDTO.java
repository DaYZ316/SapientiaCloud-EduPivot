package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登出请求
 *
 * @author DaYZ
 * @date 2025/06/20
 */
@Data
@Schema(description = "用户登出请求")
public class LogoutDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5667500266810005408L;

    /**
     * 登出类型：current-当前设备, all-所有设备
     */
    @Schema(description = "登出类型", example = "current", allowableValues = {"current", "all"})
    private String logoutType = "current";

    /**
     * 客户端信息（可选）
     */
    @Schema(description = "客户端信息", example = "Web Browser")
    private String clientInfo;

    /**
     * 设备标识（可选）
     */
    @Schema(description = "设备标识", example = "web-12345")
    private String deviceId;
} 