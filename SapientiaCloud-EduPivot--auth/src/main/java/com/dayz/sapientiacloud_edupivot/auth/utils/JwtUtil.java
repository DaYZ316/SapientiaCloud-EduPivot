package com.dayz.sapientiacloud_edupivot.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.dayz.sapientiacloud_edupivot.auth.config.JwtProperties;
import com.dayz.sapientiacloud_edupivot.auth.exception.JwtAuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_PREFIX = "auth:token:";

    /**
     * 生成JWT token
     */
    public String generateToken(UUID userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        String token = JWT.create()
                .withIssuer("SapientiaCloud-EduPivot")
                .withSubject(username)
                .withClaim("userId", userId.toString())
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));

        // 将token存入Redis
        redisTemplate.opsForValue().set(
                TOKEN_PREFIX + userId,
                token,
                jwtProperties.getExpiration(),
                TimeUnit.SECONDS
        );

        return token;
    }

    /**
     * 解析JWT token
     */
    public DecodedJWT parseToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .withIssuer("SapientiaCloud-EduPivot")
                    .build();
            return verifier.verify(token);
        } catch (Exception e) {
            log.error("JWT token解析失败: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid token", e);
        }
    }

    /**
     * 验证token有效性
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = parseToken(token);
            UUID userId = getUserId(token);

            // 检查Redis中是否存在该token
            String redisToken = (String) redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
            return token.equals(redisToken);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取token中的用户ID
     */
    public UUID getUserId(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        return UUID.fromString(decodedJWT.getClaim("userId").asString());
    }

    /**
     * 获取token中的用户名
     */
    public String getUsername(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getClaim("username").asString();
    }

    /**
     * 注销token
     */
    public void logout(String token) {
        try {
            UUID userId = getUserId(token);
            redisTemplate.delete(TOKEN_PREFIX + userId);
        } catch (Exception e) {
            log.error("注销token失败: {}", e.getMessage());
        }
    }
}