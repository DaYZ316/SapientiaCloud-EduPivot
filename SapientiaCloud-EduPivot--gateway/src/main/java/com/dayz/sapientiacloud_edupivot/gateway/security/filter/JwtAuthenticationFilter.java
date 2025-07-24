package com.dayz.sapientiacloud_edupivot.gateway.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.gateway.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(10) // 确保JWT认证过滤器优先执行
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;

    private static final String[] WHITELIST = {
            "/api/auth/login",
            "/api/auth/validate",
            "/api/auth/register",
            "/api/*/v3/api-docs/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**"
    };
    
    private static final String USERID_CLAIM = "userId";
    private static final String USERNAME_CLAIM = "username";
    private static final String ROLEKEYS_CLAIM = "roleKeys";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    // 自定义请求头
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_NAME = "X-User-Name";
    private static final String X_USER_ROLES = "X-User-Roles";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();
        
        // 如果是白名单中的路径，直接放行
        if (isWhitelistPath(requestPath)) {
            return chain.filter(exchange);
        }

        // 提取请求头中的Token
        String token = extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    String username = jwt.getSubject();
                    String userId = jwt.getClaim(USERID_CLAIM).asString();

                    if (username != null && userId != null) {
                        // 从令牌中获取角色
                        List<String> roleKeys = jwtUtil.getRoleKeysFromToken(token);

                        // 创建用户详情对象，存储用户名和用户ID
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put(USERNAME_CLAIM, username);
                        userDetails.put(USERID_CLAIM, userId);
                        
                        // 获取原始的授权头
                        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                        
                        // 添加用户信息到请求头，传递给下游服务
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header(AUTHORIZATION_HEADER, authHeader)
                                .header(X_USER_ID, userId)
                                .header(X_USER_NAME, username)
                                .header(X_USER_ROLES, String.join(",", roleKeys))
                                .build();
                        
                        log.info("传递授权头到下游服务");
                        log.info("X_USER_ID: {}", userId);
                        log.info("X_USER_NAME: {}", username);
                        log.info("X_USER_ROLES: {}", String.join(",", roleKeys));
                        
                        exchange = exchange.mutate().request(mutatedRequest).build();

                        // 创建认证对象并设置到上下文
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roleKeys.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList())
                        );

                        // 设置认证信息到反应式安全上下文
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    }
                }
            } catch (Exception e) {
                log.error("JWT令牌验证失败: {}", e.getMessage());
            }
        } else {
            log.warn("请求未携带JWT令牌: {}", requestPath);
        }

        // 继续处理请求，即使认证失败
        // 安全配置会处理未认证的请求
        return chain.filter(exchange);
    }

    private boolean isWhitelistPath(String requestPath) {
        for (String pattern : WHITELIST) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (StringUtils.hasText(bearerToken)) {
                // 如果已经有Bearer前缀，则去掉前缀
                if (bearerToken.startsWith(BEARER_PREFIX)) {
                    return bearerToken.substring(BEARER_PREFIX.length());
                }
                // 如果没有Bearer前缀，直接返回token
                return bearerToken;
            }
        }
        return null;
    }
} 