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
    OAUTH2_CONFIG_ERROR(5007, "OAuth2配置错误，请检查配置文件"),
    OAUTH2_USER_INFO_INVALID(5008, "GitHub用户信息获取失败或格式错误"),
    OAUTH2_NETWORK_ERROR(5009, "网络请求失败，请稍后重试"),
    OAUTH2_TOKEN_INVALID(5010, "访问令牌无效或已过期"),
    OAUTH2_PERMISSION_DENIED(5011, "权限不足，无法获取用户信息"),
    OAUTH2_API_ERROR(5012, "第三方服务接口错误"),
    OAUTH2_RESPONSE_INVALID(5013, "第三方服务响应数据格式错误");

    private final int code;

    private final String message;
}

