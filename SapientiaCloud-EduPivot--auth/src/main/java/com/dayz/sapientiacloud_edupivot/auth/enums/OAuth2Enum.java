package com.dayz.sapientiacloud_edupivot.auth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuth2Enum implements BaseEnum {

    STATE_INVALID(5001, "无效的state参数，可能存在CSRF攻击"),
    STATE_EXPIRED(5002, "state参数已过期，请重新发起授权"),
    STATE_NOT_FOUND(5003, "state参数不存在或已被使用"),
    PROVIDER_NOT_SUPPORTED(5004, "不支持的第三方登录提供商"),
    AUTHORIZATION_CODE_EXPIRED(5005, "授权码已过期或已被使用"),
    OAUTH2_CALLBACK_FAILED(5006, "OAuth2回调处理失败"),
    OAUTH2_CONFIG_ERROR(5007, "OAuth2配置错误，请检查配置文件");

    private final int code;

    private final String message;
}

