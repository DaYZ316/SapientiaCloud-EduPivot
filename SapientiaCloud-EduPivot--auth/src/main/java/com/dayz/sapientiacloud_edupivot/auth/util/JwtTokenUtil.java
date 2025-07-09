package com.dayz.sapientiacloud_edupivot.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * JWT 工具类 (使用 auth0:java-jwt)
 *
 * @author DaYZ
 * @date 2025/06/20
 */
@Slf4j
@Component
public class JwtTokenUtil {

    private final Algorithm algorithm;

    private final Long expiration;

    private final JWTVerifier verifier;

    public JwtTokenUtil(
            @Value("${jwt.secret:zhaosheng123}") String secret,
            @Value("${jwt.expiration:#{T(com.dayz.sapientiacloud_edupivot.auth.constants.SecurityConstants).TOKEN_EXPIRE_TIME}}") Long expiration) {
        this.algorithm = Algorithm.HMAC512(secret);
        this.expiration = expiration;
        this.verifier = JWT.require(this.algorithm).build();
    }

    public String generateToken(UserDetails userDetails) {
        var now = Instant.now();
        var expiry = now.plusMillis(this.expiration);

        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(expiry)
                .sign(algorithm);
    }

    public String getUsernameFromToken(String token) throws JWTVerificationException {
        return verifier.verify(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        var username = userDetails.getUsername();
        try {
            // 构建一个要求 subject 必须匹配的特定验证器
            var specificVerifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build();

            specificVerifier.verify(token);
            return true;
        } catch (JWTVerificationException exception) {
            log.warn("用户'{}'的Token校验失败: {}", username, exception.getMessage());
            return false;
        }
    }
}