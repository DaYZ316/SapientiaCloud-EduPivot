package com.dayz.sapientiacloud_edupivot.gateway.security.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dayz.sapientiacloud_edupivot.gateway.security.constants.JwtConstants;
import com.dayz.sapientiacloud_edupivot.gateway.security.utils.JwtUtil;
import com.dayz.sapientiacloud_edupivot.gateway.security.utils.TokenExtractionUtil;
import com.dayz.sapientiacloud_edupivot.gateway.security.utils.WhitelistUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(10) // 确保JWT认证过滤器优先执行
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private static final String[] WHITELIST = {
            "/api/auth/login",
            "/api/auth/mobile-login",
            "/api/auth/validate",
            "/api/auth/register",
            "/api/auth/send-code",
            "/api/auth/check-username",
            "/api/auth/check-mobile",
            "/api/auth/bind-mobile",
            "/api/auth/bind-mobile/confirm",
            "/api/auth/github/**",
            "/api/auth/oauth2/**",
            "/github/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/*/v3/api-docs/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**",
            "/favicon.ico"
    };
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();

        // 如果是白名单中的路径，直接放行
        if (WhitelistUtil.isWhitelistPath(requestPath, WHITELIST)) {
            return chain.filter(exchange);
        }

        // 提取请求头中的Token
        String token = TokenExtractionUtil.extractTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                // 验证令牌，此方法会检查令牌是否在黑名单中
                if (!jwtUtil.isTokenExpired(token)) {
                    DecodedJWT jwt = jwtUtil.validateToken(token);
                    String username = jwt.getSubject();
                    String userId = jwt.getClaim(JwtConstants.USERID_CLAIM).asString();

                    if (username != null && userId != null) {
                        // 从令牌中获取角色
                        List<String> roleKeys = jwtUtil.getRoleKeysFromToken(token);

                        // 创建用户详情对象，存储用户名和用户ID
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put(JwtConstants.USERNAME_CLAIM, username);
                        userDetails.put(JwtConstants.USERID_CLAIM, userId);

                        // 获取原始的授权头
                        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

                        // 添加用户信息到请求头，传递给下游服务
                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header(JwtConstants.AUTHORIZATION_HEADER, authHeader)
                                .header(JwtConstants.X_USER_ID, userId)
                                .header(JwtConstants.X_USER_NAME, username)
                                .header(JwtConstants.X_USER_ROLES, String.join(",", roleKeys))
                                .build();

                        log.debug("传递授权头到下游服务 - X_USER_ID: {}, X_USER_NAME: {}, X_USER_ROLES: {}",
                                userId, username, String.join(",", roleKeys));

                        exchange = exchange.mutate().request(mutatedRequest).build();

                        // 创建认证对象并设置到上下文
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                roleKeys.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .toList()
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

}