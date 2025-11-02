package com.dayz.sapientiacloud_edupivot.auth.security.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;

/**
 * Web相关配置类（CORS、RestTemplate等）
 */
@Configuration
@Slf4j
public class WebConfig {

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
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);
        
        // 配置代理：端口7890，通常用于本地代理服务（如Clash、V2Ray等）
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 7890));
        factory.setProxy(proxy);
        log.info("已配置OAuth请求代理: localhost:7890");

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(@NotNull java.net.URI url, @NotNull HttpMethod method,
                                    @NotNull ClientHttpResponse response) throws IOException {
                log.warn("HTTP请求失败: {} {}, 状态码: {}", method, url, response.getStatusCode());
            }
        });

        return restTemplate;
    }
}

