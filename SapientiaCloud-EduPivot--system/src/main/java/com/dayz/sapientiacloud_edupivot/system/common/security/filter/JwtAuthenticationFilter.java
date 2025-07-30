package com.dayz.sapientiacloud_edupivot.system.common.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.system.common.security.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] WHITELIST = {
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**",
    };
    private static final String USERID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Integer TOKEN_PREFIX_LENGTH = BEARER_PREFIX.length();
    // 网关传递的自定义请求头
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    private static final String X_USER_ROLES = "X-User-Roles";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 如果是白名单中的路径，直接放行
        if (isWhitelistPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 首先尝试从自定义请求头中获取用户信息
        String userId = request.getHeader(X_USER_ID);
        String username = request.getHeader(X_USER_NAME);
        String rolesStr = request.getHeader(X_USER_ROLES);

        if (StringUtils.hasText(userId) && StringUtils.hasText(username)) {
            // 从请求头中获取到用户信息，直接使用
            log.debug("从网关传递的自定义请求头中获取用户信息成功");

            // 解析角色信息
            List<String> roleKeys = new ArrayList<>();
            if (StringUtils.hasText(rolesStr)) {
                roleKeys = Arrays.asList(rolesStr.split(","));
            }

            // 创建用户详情对象
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put(USERNAME_CLAIM, username);
            userDetails.put(USERID_CLAIM, userId);

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

            filterChain.doFilter(request, response);
            return;
        }

        // 如果没有从自定义请求头获取到用户信息，则尝试从JWT令牌中获取
        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    username = jwt.getSubject();
                    userId = jwt.getClaim(USERID_CLAIM).asString();

                    if (username != null && userId != null) {
                        // 从令牌中获取角色
                        List<String> roleKeys = jwtUtil.getRoleKeysFromToken(token);

                        // 创建用户详情对象，存储用户名和用户ID
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put(USERNAME_CLAIM, username);
                        userDetails.put(USERID_CLAIM, userId);

                        // 创建认证对象，使用userDetails作为principal
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roleKeys.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList())
                        );

                        // 设置认证信息到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("用户 {} (ID: {}) 已认证通过JWT令牌", username, userId);
                    }
                }
            } catch (Exception e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.debug("请求未携带JWT令牌或自定义请求头: {}", requestURI);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelistPath(String requestURI) {
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken)) {
            // 如果已经有Bearer前缀，则去掉前缀
            if (bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(TOKEN_PREFIX_LENGTH);
            }
            // 如果没有Bearer前缀，直接返回token
            return bearerToken;
        }
        return null;
    }
} 