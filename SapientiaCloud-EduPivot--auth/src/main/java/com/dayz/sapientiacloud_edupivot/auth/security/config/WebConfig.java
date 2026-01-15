package com.dayz.sapientiacloud_edupivot.auth.security.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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
    public RestTemplate restTemplate(@Value("${app.http.proxy.enabled:false}") boolean proxyEnabled,
                                     @Value("${app.http.proxy.host:}") String proxyHost,
                                     @Value("${app.http.proxy.port:0}") int proxyPort) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);
        boolean hasValidHost = proxyHost != null && !proxyHost.isBlank() && proxyPort > 0;
        if (proxyEnabled && hasValidHost) {
            try {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                factory.setProxy(proxy);
                log.info("RestTemplate proxy enabled via config: {}:{}", proxyHost, proxyPort);
            } catch (Exception e) {
                log.warn("配置代理失败，已忽略，使用直连: {}:{}", proxyHost, proxyPort, e);
            }
        } else if (proxyEnabled) {
            log.warn("代理开关已启用，但未配置有效的 host/port，使用直连");
        } else {
            log.debug("RestTemplate proxy disabled by config, using direct connections");
        }

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

