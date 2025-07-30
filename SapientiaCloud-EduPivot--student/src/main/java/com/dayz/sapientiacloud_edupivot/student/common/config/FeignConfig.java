package com.dayz.sapientiacloud_edupivot.student.common.config;

import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
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

                // 添加用户信息到请求头
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
                            log.debug("获取用户ID失败: {}", e.getMessage());
                        }

                        // 获取当前用户名
                        try {
                            String username = UserContextUtil.getCurrentUsername();
                            if (username != null) {
                                requestTemplate.header(X_USER_NAME, username);
                            }
                        } catch (Exception e) {
                            log.debug("获取用户名失败: {}", e.getMessage());
                        }

                        // 获取当前用户角色
                        try {
                            List<String> roles = UserContextUtil.getCurrentUserRoles();
                            if (roles != null && !roles.isEmpty()) {
                                requestTemplate.header(X_USER_ROLES, String.join(",", roles));
                            }
                        } catch (Exception e) {
                            log.debug("获取用户角色失败: {}", e.getMessage());
                        }

                        log.debug("Feign请求添加用户信息请求头成功");
                    }
                } catch (Exception e) {
                    log.warn("添加用户信息到Feign请求头失败: {}", e.getMessage());
                }
            }
        };
    }
} 