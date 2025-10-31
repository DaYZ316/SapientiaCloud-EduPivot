package com.dayz.sapientiacloud_edupivot.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.github")
public class GitHubOAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
}