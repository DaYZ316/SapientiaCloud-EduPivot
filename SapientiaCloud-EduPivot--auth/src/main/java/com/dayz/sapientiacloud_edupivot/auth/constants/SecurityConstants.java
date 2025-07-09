package com.dayz.sapientiacloud_edupivot.auth.constants;

/**
 * 安全相关常量
 *
 * @author DaYZ
 * @date 2025/06/20
 */
public interface SecurityConstants {

    /**
     * 认证请求头
     */
    String AUTH_HEADER = "Authorization";

    /**
     * Token前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 密码加密算法
     */
    String BCRYPT = "{bcrypt}";

    /**
     * 内部接口请求头
     */
    String INNER_HEADER = "X-Inner-Call";

    /**
     * 内部接口请求头值
     */
    String INNER_VALUE = "TRUE";

    /**
     * Token过期时间（毫秒）- 24小时
     */
    long TOKEN_EXPIRE_TIME = 86400000;

    /**
     * 用户ID标识
     */
    String USER_ID = "userId";

    /**
     * 用户名标识
     */
    String USERNAME = "username";

    /**
     * 角色标识
     */
    String AUTHORITIES = "authorities";
} 