package com.dayz.sapientiacloud_edupivot.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * JWT密钥
     */
    private String secret = "SapientiaCloud-EduPivot-Secret-Key-2024";
    
    /**
     * JWT过期时间（秒）
     */
    private Long expiration = 7200L;
    
    /**
     * 刷新token过期时间（秒）
     */
    private Long refreshExpiration = 604800L;
    
    /**
     * JWT请求头
     */
    private String header = "Authorization";
    
    /**
     * JWT前缀
     */
    private String prefix = "Bearer ";
}