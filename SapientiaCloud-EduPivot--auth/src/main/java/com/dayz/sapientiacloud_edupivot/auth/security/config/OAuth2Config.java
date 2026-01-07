package com.dayz.sapientiacloud_edupivot.auth.security.config;

import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OAuth2配置属性类
 * 注意：这是一个纯配置属性类，不包含Bean定义，Bean定义在WebConfig中
 */
@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.github")
public class OAuth2Config {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private List<String> scope;

    /**
     * 初始化后验证配置是否加载成功
     * 配置错误时直接抛出异常，确保应用启动时就能发现问题
     */
    @PostConstruct
    public void validate() {
        if (clientId == null || clientId.isBlank()) {
            log.error("GitHub OAuth2 clientId 未配置！请检查配置文件中的 spring.security.oauth2.client.registration.github.client-id");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            log.error("GitHub OAuth2 clientSecret 未配置！请检查配置文件中的 spring.security.oauth2.client.registration.github.client-secret");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            log.error("GitHub OAuth2 redirectUri 未配置！请检查配置文件中的 spring.security.oauth2.client.registration.github.redirect-uri");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (scope == null || scope.isEmpty()) {
            log.error("GitHub OAuth2 scope 未配置！请检查配置文件中的 spring.security.oauth2.client.registration.github.scope");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        log.debug("GitHub OAuth2 Registration配置加载完成: clientId={}, redirectUri={}, scope={}",
                clientId.substring(0, Math.min(10, clientId.length())) + "...",
                redirectUri, String.join(",", scope));
    }
}