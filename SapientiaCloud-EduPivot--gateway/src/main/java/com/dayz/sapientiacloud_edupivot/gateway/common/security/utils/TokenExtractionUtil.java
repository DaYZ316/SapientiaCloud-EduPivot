package com.dayz.sapientiacloud_edupivot.gateway.common.security.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.dayz.sapientiacloud_edupivot.gateway.common.security.constants.JwtConstants.*;

/**
 * Token提取工具类
 * 支持 Reactive 方式
 */
public class TokenExtractionUtil {

    /**
     * 从 Reactive 请求中提取 Token
     *
     * @param request ServerHttpRequest
     * @return Token字符串，如果不存在则返回null
     */
    public static String extractTokenFromRequest(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return extractToken(authHeaders.get(0));
        }
        return null;
    }

    /**
     * 从 Bearer Token 字符串中提取实际的 Token
     *
     * @param bearerToken Bearer Token字符串
     * @return Token字符串，如果不存在则返回null
     */
    private static String extractToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken)) {
            // 如果已经有Bearer前缀，则去掉前缀
            if (bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(BEARER_PREFIX_LENGTH);
            }
            // 如果没有Bearer前缀，直接返回token
            return bearerToken;
        }
        return null;
    }

    /**
     * 提取实际的JWT令牌（去掉Bearer前缀）
     *
     * @param token Token字符串（可能包含Bearer前缀）
     * @return 实际的JWT令牌字符串
     */
    public static String extractActualToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER_PREFIX)) {
            return token.substring(BEARER_PREFIX_LENGTH);
        }
        return token;
    }

    private TokenExtractionUtil() {
        // 禁止实例化
    }
}
