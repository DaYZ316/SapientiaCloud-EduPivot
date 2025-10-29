package com.dayz.sapientiacloud_edupivot.auth.security.config;

import com.dayz.sapientiacloud_edupivot.auth.security.filter.JwtAuthenticationFilter;
import com.dayz.sapientiacloud_edupivot.auth.security.handler.OAuth2SuccessHandler;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用基本配置
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(login -> login.permitAll()) // 允许表单登录，为了支持OAuth2的登录页面
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // 为了OAuth2流程，暂时允许会话状态
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // 所有认证和OAuth2相关路径都允许访问
                        .requestMatchers(
                            "/login", 
                            "/mobile-login", 
                            "/validate", 
                            "/register",
                            "/oauth2/**", 
                            "/login/oauth2/**",
                            "/v3/api-docs/**", 
                            "/doc.html", 
                            "/webjars/**"
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

    @Bean
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver = 
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorize");
        
        return resolver;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.setAllowedOrigins(Collections.singletonList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        corsConfig.setExposedHeaders(Arrays.asList("Content-Length", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsFilter(source);
    }
    
    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        // 配置HTTP客户端超时设置
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 连接超时10秒
        factory.setReadTimeout(30000);    // 读取超时30秒
        
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