package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登出响应
 *
 * @author DaYZ
 * @date 2025/06/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登出响应")
public class LogoutVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 542160515879595370L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 登出时间
     */
    @Schema(description = "登出时间")
    private LocalDateTime logoutTime;

    /**
     * 登出类型
     */
    @Schema(description = "登出类型", example = "current")
    private String logoutType;

    /**
     * 失效的Token数量
     */
    @Schema(description = "失效的Token数量", example = "1")
    private Integer invalidatedTokenCount;

    /**
     * 消息
     */
    @Schema(description = "登出消息", example = "登出成功")
    private String message;
} 