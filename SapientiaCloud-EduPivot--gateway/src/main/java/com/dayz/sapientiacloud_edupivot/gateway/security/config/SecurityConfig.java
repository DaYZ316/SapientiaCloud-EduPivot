package com.dayz.sapientiacloud_edupivot.gateway.security.config;

import com.dayz.sapientiacloud_edupivot.gateway.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    private static final String[] WHITELIST = {
            "/api/auth/login",
            "/api/auth/validate",
            "/api/auth/logout",
            "/api/system/user/register",
            "/api/*/v3/api-docs/**",
            "/v3/api-docs/**",
            "/doc.html",
            "/webjars/**"
    };
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // 禁用CSRF
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // 禁用formLogin
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            // 禁用httpBasic
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            // 配置请求授权
            .authorizeExchange(exchange -> exchange
                // 跨域预检请求 - 放行所有OPTIONS请求
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 白名单放行
                .pathMatchers(WHITELIST).permitAll()
                // 需要认证的请求
                .anyExchange().authenticated()
            )
            // 添加JWT过滤器
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            // 设置未授权处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 