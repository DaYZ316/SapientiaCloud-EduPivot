package com.dayz.sapientiacloud_edupivot.gateway.security.config;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.gateway.result.Result;
import com.dayz.sapientiacloud_edupivot.gateway.result.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        ServerHttpResponse response = exchange.getResponse();
        
        // 设置响应状态码为401
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 设置内容类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // 创建错误响应
        Result<Void> result = Result.fail(ResultEnum.UNAUTHORIZED);
        String body = JSON.toJSONString(result);
        
        // 写入响应
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
} 