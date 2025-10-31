package com.dayz.sapientiacloud_edupivot.auth.security.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.security.config.JwtConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String TOKEN_BLACKLIST_PREFIX = com.dayz.sapientiacloud_edupivot.auth.common.security.constants.JwtConstants.TOKEN_BLACKLIST_PREFIX;
    private static final String REFRESH_TOKEN_PREFIX = com.dayz.sapientiacloud_edupivot.auth.common.security.constants.JwtConstants.REFRESH_TOKEN_PREFIX;
    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, Object> redisTemplate;

    public String generateToken(SysUserInternalVO sysUserInternalVO) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration());
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
        }

        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId", sysUserInternalVO.getId().toString())
                .withSubject(sysUserInternalVO.getUsername())
                .withClaim("roleKeys", sysUserInternalVO.getRoles() != null ? 
                        sysUserInternalVO.getRoles().stream()
                                .filter(Objects::nonNull)
                                .map(SysRoleVO::getRoleKey)
                                .toList() : List.of())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    public String generateRefreshToken(SysUserInternalVO sysUserInternalVO) {
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
        }
        // 生成刷新令牌，有效期设置为7天
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000L);
        String refreshToken = JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("userId", sysUserInternalVO.getId().toString())
                .withSubject(sysUserInternalVO.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(jwtConfig.getSecret() + "-refresh"));
        // 保存刷新令牌到Redis，用于验证
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + sysUserInternalVO.getId();
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken, 7, TimeUnit.DAYS);
        return refreshToken;
    }

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

    public String getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getClaim("userId").asString();
        } catch (JWTVerificationException e) {
            log.error("无法从令牌中获取用户ID", e);
            return null;
        }
    }

    public List<String> getRoleKeysFromToken(String token) {
        try {
            DecodedJWT jwt = validateToken(token);
            return jwt.getClaim("roleKeys").asList(String.class);
        } catch (JWTVerificationException e) {
            log.error("无法从令牌中获取用户角色", e);
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

    public boolean invalidateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            String actualToken = extractActualToken(token);
            DecodedJWT jwt = JWT.decode(actualToken);
            Date expiryDate = jwt.getExpiresAt();
            long ttl = Math.max(0, expiryDate.getTime() - System.currentTimeMillis());
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + actualToken;
            redisTemplate.opsForValue().set(blacklistKey, "invalidated", ttl, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            log.error("无法销毁令牌", e);
            return false;
        }
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.hasText(bearerToken)) {
            throw new BusinessException("令牌为空或格式错误");
        }
        return bearerToken;
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
        // 使用工具类提取实际token
        return com.dayz.sapientiacloud_edupivot.auth.common.security.utils.TokenExtractionUtil.extractActualToken(token);
    }
}