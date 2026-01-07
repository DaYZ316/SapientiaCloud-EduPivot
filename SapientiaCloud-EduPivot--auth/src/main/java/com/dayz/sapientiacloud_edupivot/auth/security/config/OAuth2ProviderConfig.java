package com.dayz.sapientiacloud_edupivot.auth.security.config;

import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth2 Provider配置属性类
 * 支持自定义OAuth2提供者配置（如通过Cloudflare Worker中转）
 */
@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.github")
public class OAuth2ProviderConfig {
    private String authorizationUri;
    private String tokenUri;
    private String userInfoUri;
    private String userNameAttribute;

    /**
     * 初始化后验证配置是否加载成功
     * 配置错误时直接抛出异常，确保应用启动时就能发现问题
     */
    @PostConstruct
    public void validate() {
        if (authorizationUri == null || authorizationUri.isBlank()) {
            log.error("GitHub OAuth2 authorizationUri 未配置！请检查配置文件中的 spring.security.oauth2.client.provider.github.authorization-uri");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (tokenUri == null || tokenUri.isBlank()) {
            log.error("GitHub OAuth2 tokenUri 未配置！请检查配置文件中的 spring.security.oauth2.client.provider.github.token-uri");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (userInfoUri == null || userInfoUri.isBlank()) {
            log.error("GitHub OAuth2 userInfoUri 未配置！请检查配置文件中的 spring.security.oauth2.client.provider.github.user-info-uri");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        if (userNameAttribute == null || userNameAttribute.isBlank()) {
            log.error("GitHub OAuth2 userNameAttribute 未配置！请检查配置文件中的 spring.security.oauth2.client.provider.github.user-name-attribute");
            throw new BusinessException(OAuth2Enum.OAUTH2_CONFIG_ERROR);
        }
        log.debug("GitHub OAuth2 Provider配置加载完成: authorizationUri={}, tokenUri={}, userInfoUri={}, userNameAttribute={}",
                authorizationUri, tokenUri, userInfoUri, userNameAttribute);
    }
}
