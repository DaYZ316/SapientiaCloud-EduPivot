package com.dayz.sapientiacloud_edupivot.system.security.filter;

import com.dayz.sapientiacloud_edupivot.system.security.util.JwtUtil;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    
    private static final String[] WHITELIST = {
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**",
            "user/register/**",
            // 常见的通过网关转发后可能的路径形式
            "/api/system/v3/api-docs/**",
            "/api/system/user/register/**",
            // 内部接口白名单
            "/user/internal/**",
    };
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Integer TOKEN_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 如果是白名单中的路径，直接放行
        if (isWhitelistPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromRequest(request);
        if (token != null) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    // 从令牌中获取用户名
                    String username = jwtUtil.getUsernameFromToken(token);
                    if (username != null) {
                        // 从令牌中获取角色
                        List<String> roleKeys = jwtUtil.getRoleKeysFromToken(token);
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                roleKeys.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList())
                        );
                        
                        // 设置认证信息到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("用户 {} 已认证通过JWT令牌", username);
                    }
                }
            } catch (Exception e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            log.warn("请求未携带JWT令牌: {}", requestURI);
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