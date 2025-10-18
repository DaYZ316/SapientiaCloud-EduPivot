package com.dayz.sapientiacloud_edupivot.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    private static final String START_TIME_ATTR = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (log.isInfoEnabled()) {
            ServerHttpRequest request = exchange.getRequest();
            exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

            log.info("[Gateway REQ_IN] Method: {}, URI: {}, RemoteAddr: {}",
                    request.getMethod(),
                    request.getURI(),
                    request.getRemoteAddress());
        }

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            if (log.isInfoEnabled()) {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                Long startTime = exchange.getAttribute(START_TIME_ATTR);
                long duration = (startTime != null) ? (System.currentTimeMillis() - startTime) : -1;

                log.info("[Gateway REQ_OUT] Status: {}, Duration: {}ms, URI: {}",
                        response.getStatusCode(),
                        duration,
                        request.getURI());
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
