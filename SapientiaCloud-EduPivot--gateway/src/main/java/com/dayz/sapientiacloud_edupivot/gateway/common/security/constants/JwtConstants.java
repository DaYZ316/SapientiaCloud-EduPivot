package com.dayz.sapientiacloud_edupivot.gateway.common.security.constants;

/**
 * JWT相关常量
 */
public class JwtConstants {

    // JWT Claim 名称
    public static final String USERID_CLAIM = "userId";
    public static final String USERNAME_CLAIM = "username";
    public static final String ROLEKEYS_CLAIM = "roleKeys";

    // HTTP 请求头
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = BEARER_PREFIX.length();

    // 自定义请求头（网关传递给下游服务）
    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_NAME = "X-User-Name";
    public static final String X_USER_ROLES = "X-User-Roles";

    // Redis 键前缀
    public static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    private JwtConstants() {
        // 禁止实例化
    }
}
