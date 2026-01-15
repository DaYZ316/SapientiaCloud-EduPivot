package com.dayz.sapientiacloud_edupivot.auth.config;

import com.dayz.sapientiacloud_edupivot.auth.security.utils.UserContextUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
public class FeignConfig {

    // Feign请求头标识
    private static final String FEIGN_REQUEST_HEADER = "X-Feign-Client";

    // 用户信息请求头
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    private static final String X_USER_ROLES = "X-User-Roles";

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 添加Feign标识头
                requestTemplate.header(FEIGN_REQUEST_HEADER, "true");

                // 添加用户信息到请求头（可选，失败不影响请求）
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        // 获取当前用户ID
                        try {
                            UUID userId = UserContextUtil.getCurrentUserId();
                            if (userId != null) {
                                requestTemplate.header(X_USER_ID, userId.toString());
                            }
                        } catch (Exception e) {
                            // 静默失败，这是正常的（如 OAuth2 回调时用户还未登录）
                            log.debug("Feign 请求添加用户ID失败（可忽略）: {}", e.getMessage());
                        }

                        // 获取当前用户名
                        try {
                            String username = UserContextUtil.getCurrentUsername();
                            if (username != null) {
                                requestTemplate.header(X_USER_NAME, username);
                            }
                        } catch (Exception e) {
                            log.debug("Feign 请求添加用户名失败（可忽略）: {}", e.getMessage());
                        }

                        // 获取当前用户角色
                        try {
                            List<String> roles = UserContextUtil.getCurrentUserRoles();
                            if (roles != null && !roles.isEmpty()) {
                                requestTemplate.header(X_USER_ROLES, String.join(",", roles));
                            }
                        } catch (Exception e) {
                            log.debug("Feign 请求添加用户角色失败（可忽略）: {}", e.getMessage());
                        }
                    } else {
                        log.debug("Feign 请求时用户未认证（这是正常的，如 OAuth2 回调）");
                    }
                } catch (Exception e) {
                    log.debug("Feign 请求添加用户信息失败（可忽略）: {}", e.getMessage());
                }
            }
        };
    }
} 