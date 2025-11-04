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
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .oauth2Login(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/mobile-login",
                                "/validate",
                                "/register",
                                "/send-code",
                                "/check-username",
                                "/check-mobile",
                                "/bind-mobile",
                                "/oauth2/**",
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/doc.html",
                                "/webjars/**",
                                "/oauth2/**"
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}