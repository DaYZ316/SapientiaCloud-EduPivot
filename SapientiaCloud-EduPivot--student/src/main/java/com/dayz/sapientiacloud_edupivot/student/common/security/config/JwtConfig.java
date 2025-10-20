package com.dayz.sapientiacloud_edupivot.student.common.security.config;

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

    private String secret = "SapientiaCloud-EduPivot-Secret";

    private long expiration = 7200000;

} 