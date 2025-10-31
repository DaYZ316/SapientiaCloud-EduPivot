package com.dayz.sapientiacloud_edupivot.auth.security.config;

import com.dayz.sapientiacloud_edupivot.auth.security.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // 仅保留 JWT 过滤相关依赖

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用基本配置
                .csrf(AbstractHttpConfigurer::disable)
                // 禁用表单登录，避免触发默认认证流程导致循环调用
                // OAuth2 使用 OAuth2LoginConfigurer，不需要表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 为了OAuth2流程，暂时允许会话状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                            "/login",
                            "/mobile-login",
                            "/validate",
                            "/register",
                            "/api/auth/**",
                            "/v3/api-docs/**",
                            "/doc.html",
                            "/webjars/**",
                            "/oauth2/**",
                            "/login/oauth2/**",
                            "/oauth2/authorization/**"
                        ).permitAll()
                        // 允许OPTIONS请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 设置未授权处理 - 使用简单的处理方式，避免显示Basic认证对话框
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"code\":401,\"message\":\"未授权访问\",\"success\":false}");
                        })
                );

        // 添加JWT过滤器，确保JWT认证正常工作
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 AuthenticationManager
     * 注意：由于使用 JWT 认证，实际上不需要这个 Bean
     * 但如果其他组件需要，可以保留
     * 由于禁用了表单登录，不会触发默认认证流程，因此不会导致循环调用
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 手动控制 OAuth2 流程后，默认的 AuthorizationRequestResolver 不再需要。

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        corsConfig.setExposedHeaders(Arrays.asList("Content-Length", "Authorization"));
        corsConfig.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }
    
    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        // 配置HTTP客户端超时设置
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        
        org.springframework.web.client.RestTemplate restTemplate = 
            new org.springframework.web.client.RestTemplate(factory);
        
        // 设置错误处理器
        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler() {
            @Override
            public void handleError(java.net.URI url, org.springframework.http.HttpMethod method, 
                                  org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
                // 记录错误但不抛出异常，让调用方处理
                log.warn("HTTP请求失败: {} {}, 状态码: {}", method, url, response.getStatusCode());
            }
        });
        
        return restTemplate;
    }
}