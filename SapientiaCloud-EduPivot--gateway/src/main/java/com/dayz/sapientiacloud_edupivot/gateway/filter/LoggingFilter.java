package com.dayz.sapientiacloud_edupivot.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * 日志过滤器
 * 记录所有请求的路径和响应状态
 */
@Slf4j
@Component
@Order(1) // 最高优先级，确保最先执行
public class LoggingFilter implements WebFilter {

    // 定义不应该完整记录的敏感头信息
    private static final Set<String> SENSITIVE_HEADERS =
            Collections.unmodifiableSet(Set.of("Authorization", "Cookie", "Set-Cookie"));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String method = request.getMethod().name();

        // 在请求开始前记录日志
        log.info("请求开始 - {} {}", method, requestUri);

        // 记录请求头信息（仅开发环境使用）
        if (log.isDebugEnabled()) {
            request.getHeaders().forEach((name, values) -> {
                if (shouldLogHeader(name)) {
                    values.forEach(value -> log.debug("请求头: {} = {}", name, value));
                } else {
                    log.debug("请求头: {} = [敏感信息已隐藏]", name);
                }
            });
        }

        // 获取请求开始时间
        long startTime = System.currentTimeMillis();

        // 继续处理请求并在响应完成后添加日志
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 计算请求耗时
            long duration = System.currentTimeMillis() - startTime;

            ServerHttpResponse response = exchange.getResponse();
            int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

            // 记录响应信息
            log.info("请求完成 - {} {} - 状态码: {} - 耗时: {}ms", method, requestUri, statusCode, duration);
        }));
    }

    private boolean shouldLogHeader(String headerName) {
        return !SENSITIVE_HEADERS.contains(headerName);
    }
} 