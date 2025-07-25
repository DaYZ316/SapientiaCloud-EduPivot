package com.dayz.sapientiacloud_edupivot.system.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Feign请求处理切面
 * 自动检测请求头中的Feign标识，并设置用户上下文
 */
@Aspect
@Component
@Slf4j
public class FeignRequestAspect {

    // 自定义请求头
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    private static final String X_USER_ROLES = "X-User-Roles";
    private static final String FEIGN_REQUEST_HEADER = "X-Feign-Client";

    /**
     * 定义切点，匹配内部接口
     */
    @Pointcut("@annotation(com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission) && " +
              "execution(* com.dayz.sapientiacloud_edupivot.system.feigns.*.*(..))")
    public void feignRequestPointcut() {}

    /**
     * 前置通知，处理Feign请求
     */
    @Before("feignRequestPointcut()")
    public void handleFeignRequest(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 检查是否是Feign请求
        String feignHeader = request.getHeader(FEIGN_REQUEST_HEADER);
        if (StringUtils.hasText(feignHeader) && "true".equals(feignHeader)) {
            // 从请求头中获取用户信息
            String userId = request.getHeader(X_USER_ID);
            String username = request.getHeader(X_USER_NAME);
            String rolesStr = request.getHeader(X_USER_ROLES);
            
            if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
                log.debug("从Feign请求头中获取用户信息: userId={}, username={}", userId, username);
                
                // 解析角色信息
                List<String> roleKeys = new ArrayList<>();
                if (StringUtils.hasText(rolesStr)) {
                    roleKeys = Arrays.asList(rolesStr.split(","));
                }
                
                // 创建用户详情对象
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("username", username);
                userDetails.put("userId", userId);
                
                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        roleKeys.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                );
                
                // 设置认证信息到安全上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("已设置Feign请求的用户认证信息到SecurityContext");
            }
        }
    }
} 