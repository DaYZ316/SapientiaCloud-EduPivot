package com.dayz.sapientiacloud_edupivot.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.dayz.sapientiacloud_edupivot.auth.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    
    /**
     * 生成JWT token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);
        
        String token = JWT.create()
                .withIssuer("SapientiaCloud-EduPivot")
                .withSubject(username)
                .withClaim("userId", userId)
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
     * 生成刷新token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration() * 1000);
        
        String refreshToken = JWT.create()
                .withIssuer("SapientiaCloud-EduPivot")
                .withClaim("userId", userId)
                .withClaim("type", "refresh")
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
        
        // 将刷新token存入Redis
        redisTemplate.opsForValue().set(
            REFRESH_TOKEN_PREFIX + userId, 
            refreshToken, 
            jwtProperties.getRefreshExpiration(), 
            TimeUnit.SECONDS
        );
        
        return refreshToken;
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
            throw new RuntimeException("Invalid token");
        }
    }
    
    /**
     * 验证token有效性
     */
    public boolean validateToken(String token) {
        try {
            DecodedJWT decodedJWT = parseToken(token);
            Long userId = decodedJWT.getClaim("userId").asLong();
            
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
    public Long getUserId(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getClaim("userId").asLong();
    }
    
    /**
     * 获取token中的用户名
     */
    public String getUsername(String token) {
        DecodedJWT decodedJWT = parseToken(token);
        return decodedJWT.getClaim("username").asString();
    }
    
    /**
     * 刷新token
     */
    public String refreshToken(String refreshToken, Long userId, String username) {
        try {
            DecodedJWT decodedJWT = parseToken(refreshToken);
            String type = decodedJWT.getClaim("type").asString();
            
            if (!"refresh".equals(type)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            Long tokenUserId = decodedJWT.getClaim("userId").asLong();
            
            // 验证刷新token是否存在于Redis
            String redisRefreshToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + tokenUserId);
            if (!refreshToken.equals(redisRefreshToken)) {
                throw new RuntimeException("Refresh token not found or expired");
            }
            
            // 生成新的token
            return generateToken(userId, username);
            
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage());
            throw new RuntimeException("Invalid refresh token");
        }
    }
    
    /**
     * 注销token
     */
    public void logout(String token) {
        try {
            Long userId = getUserId(token);
            redisTemplate.delete(TOKEN_PREFIX + userId);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        } catch (Exception e) {
            log.error("注销token失败: {}", e.getMessage());
        }
    }
}