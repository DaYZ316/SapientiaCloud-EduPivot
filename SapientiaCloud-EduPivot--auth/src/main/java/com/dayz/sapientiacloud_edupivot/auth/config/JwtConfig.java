package com.dayz.sapientiacloud_edupivot.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret = "SapientiaCloud-EduPivot-Secret";

    /**
     * 令牌有效期（毫秒）默认2小时
     */
    private long expiration = 7200000;

} 