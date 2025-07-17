package com.dayz.sapientiacloud_edupivot.gateway.security.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.gateway.config.JwtConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig jwtConfig;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";

    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        if (isTokenInBlacklist(token)) {
            throw new JWTVerificationException("令牌已被注销");
        }

        String actualToken = extractActualToken(token);
        
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret())).build();
        return verifier.verify(actualToken);
    }

    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            log.error("无法从令牌中获取用户名", e);
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }

    private boolean isTokenInBlacklist(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        // 提取实际的JWT令牌
        String actualToken = extractActualToken(token);
        
        // 检查Redis黑名单
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + actualToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    private String extractActualToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
} 