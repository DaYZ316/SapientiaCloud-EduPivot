package com.dayz.sapientiacloud_edupivot.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.auth.config.JwtConfig;
import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    /**
     * JWT配置
     */
    private final JwtConfig jwtConfig;

    /**
     * Redis模板
     */
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 令牌黑名单前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "jwt:blacklist:";
    
    /**
     * 生成JWT令牌
     *
     * @param user 用户信息
     * @return JWT令牌
     */
    public String generateToken(SysUser user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());

        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(user.getUsername())
                .withClaim("userId", user.getId().toString())
                .withClaim("nickName", user.getNickName())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 解码后的JWT
     * @throws JWTVerificationException 验证异常
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        // 检查令牌是否在黑名单中
        if (isTokenInBlacklist(token)) {
            throw new JWTVerificationException("令牌已被注销");
        }
        
        // 提取实际的JWT令牌
        String actualToken = extractActualToken(token);
        
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret())).build();
        return verifier.verify(actualToken);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            log.error("无法从令牌中获取用户名", e);
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getClaim("userId").asString();
        } catch (JWTVerificationException e) {
            log.error("无法从令牌中获取用户ID", e);
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     *
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTVerificationException e) {
            return true;
        }
    }
    
    /**
     * 销毁JWT令牌（加入黑名单）
     *
     * @param token JWT令牌（可能带有Bearer前缀）
     * @return 是否成功销毁
     */
    public boolean invalidateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        try {
            // 提取实际的JWT令牌
            String actualToken = extractActualToken(token);
            
            // 尝试解析令牌以获取过期时间
            DecodedJWT jwt = JWT.decode(actualToken);
            Date expiryDate = jwt.getExpiresAt();
            long ttl = Math.max(0, expiryDate.getTime() - System.currentTimeMillis());
            
            // 使用Redis存储黑名单，并设置过期时间为JWT的剩余有效期
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + actualToken;
            redisTemplate.opsForValue().set(blacklistKey, "invalidated", ttl, TimeUnit.MILLISECONDS);
            log.info("令牌已添加到Redis黑名单, 剩余有效期: {}ms", ttl);
            
            return true;
        } catch (Exception e) {
            log.error("无法销毁令牌", e);
            return false;
        }
    }
    
    /**
     * 检查令牌是否在黑名单中
     *
     * @param token JWT令牌
     * @return 是否在黑名单中
     */
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
    
    /**
     * 提取实际的JWT令牌（去掉Bearer前缀）
     *
     * @param token 原始令牌
     * @return 实际JWT令牌
     */
    private String extractActualToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
} 