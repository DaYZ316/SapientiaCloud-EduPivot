package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author DaYZ
 * @date 2025/06/20
 */
@Data
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5750501015267626833L;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", example = "123456")
    private String password;
} 