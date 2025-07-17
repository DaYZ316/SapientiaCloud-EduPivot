package com.dayz.sapientiacloud_edupivot.auth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
    // Feign请求头标识
    private static final String FEIGN_REQUEST_HEADER = "X-Feign-Client";

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 添加Feign标识头
                requestTemplate.header(FEIGN_REQUEST_HEADER, "true");
            }
        };
    }
} 