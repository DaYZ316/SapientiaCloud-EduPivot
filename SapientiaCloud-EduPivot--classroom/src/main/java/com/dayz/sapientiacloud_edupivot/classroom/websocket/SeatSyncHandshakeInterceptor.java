package com.dayz.sapientiacloud_edupivot.classroom.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSyncHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${classroom.seat-sync.token.redis.prefix:classroom:seat-sync:token:}")
    private String wsTokenPrefix;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String token = queryParams.getFirst("token");
        String recordIdFromRequest = resolveRecordId(request, queryParams.getFirst("recordId"));
        if (!StringUtils.hasText(token) || !StringUtils.hasText(recordIdFromRequest)) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }

        Object entry = redisTemplate.opsForValue().get(wsTokenPrefix + token);
        if (entry == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        redisTemplate.delete(wsTokenPrefix + token);

        String boundRecordId = null;
        String boundUserId = null;
        if (entry instanceof Map<?, ?> info) {
            Object rawRecordId = info.get("recordId");
            if (rawRecordId != null) {
                boundRecordId = rawRecordId.toString();
            }
            Object rawUserId = info.get("userId");
            if (rawUserId != null) {
                boundUserId = rawUserId.toString();
            }
        }

        if (StringUtils.hasText(boundRecordId) && !boundRecordId.equals(recordIdFromRequest)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        try {
            attributes.put("recordId", UUID.fromString(recordIdFromRequest));
        } catch (IllegalArgumentException e) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        if (StringUtils.hasText(boundUserId)) {
            attributes.put("userId", boundUserId);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private String resolveRecordId(ServerHttpRequest request, String recordIdFromQuery) {
        if (StringUtils.hasText(recordIdFromQuery)) {
            return recordIdFromQuery;
        }
        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getParameter("recordId");
        }
        return null;
    }
}
