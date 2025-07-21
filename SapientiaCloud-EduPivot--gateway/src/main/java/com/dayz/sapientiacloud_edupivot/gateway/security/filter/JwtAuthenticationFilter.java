package com.dayz.sapientiacloud_edupivot.gateway.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.gateway.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    
    private static final String[] WHITELIST = {
            "/api/auth/login",
            "/api/auth/validate",
            "/api/auth/register",
            "/api/auth/v3/api-docs/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**"
    };
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Integer TOKEN_PREFIX_LENGTH = BEARER_PREFIX.length();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 如果是白名单中的路径，直接放行
        if (isWhitelistPath(path)) {
            return chain.filter(exchange);
        }
        
        // 获取token
        String token = extractTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            try {
                // 验证令牌
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    String username = jwt.getSubject();
                    
                    if (username != null) {
                        List<String> roleKeys = jwt.getClaim("roleKeys").asList(String.class);
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        
                        // 添加角色权限
                        if (roleKeys != null && !roleKeys.isEmpty()) {
                            for (String roleKey : roleKeys) {
                                authorities.add(new SimpleGrantedAuthority(roleKey));
                            }
                        }
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                            
                        // 设置认证信息到上下文
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }
                }
            } catch (Exception e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
            }
        } else {
            log.warn("请求未携带JWT令牌: {}", path);
        }
        
        return chain.filter(exchange);
    }
    
    private boolean isWhitelistPath(String requestURI) {
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get(AUTHORIZATION_HEADER);
        if (headers != null && !headers.isEmpty()) {
            String bearerToken = headers.get(0);
            if (StringUtils.hasText(bearerToken)) {
                // 如果已经有Bearer前缀，则去掉前缀
                if (bearerToken.startsWith(BEARER_PREFIX)) {
                    return bearerToken.substring(TOKEN_PREFIX_LENGTH);
                }
                // 如果没有Bearer前缀，直接返回token
                return bearerToken;
            }
        }
        return null;
    }
} 