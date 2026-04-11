package com.dayz.sapientiacloud_edupivot.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.dayz.sapientiacloud_edupivot.gateway.result.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SentinelGatewayConfig {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final String BLOCKED_MESSAGE = "Too many requests, please try again later.";

    @PostConstruct
    public void registerBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, throwable) -> ServerResponse
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Result.fail(TOO_MANY_REQUESTS, BLOCKED_MESSAGE)));
    }
}
