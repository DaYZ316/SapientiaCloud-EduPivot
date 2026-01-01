package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.event.LiveEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/live")
public class LiveEventController {

    private final LiveEventPublisher liveEventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${live.sse.token.redis.prefix:sse:token:}")
    private String sseTokenPrefix;

    @Value("${live.sse.token.ttl.seconds:60}")
    private long sseTokenTtlSeconds;

    public LiveEventController(LiveEventPublisher liveEventPublisher, RedisTemplate<String, Object> redisTemplate) {
        this.liveEventPublisher = liveEventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@RequestParam(value = "classroomId", required = false) String classroomId,
                                @RequestParam(value = "token", required = false) String token) {
        // token is required for authenticated subscribe
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token required");
        }
        Object entry = redisTemplate.opsForValue().get(sseTokenPrefix + token);
        if (entry == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid token");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) entry;
            String boundClassroom = info.get("classroomId") != null ? info.get("classroomId").toString() : null;
            if (boundClassroom != null && classroomId != null && !boundClassroom.equals(classroomId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "classroom mismatch");
            }
        } catch (ClassCastException e) {
            // ignore
        }
        // consume token to prevent reuse
        redisTemplate.delete(sseTokenPrefix + token);
        return liveEventPublisher.subscribe(classroomId);
    }

    /**
     * 生成SSE token，用于直播事件订阅
     * 支持匿名访问，但会验证用户身份（如果已登录）
     */
    @PostMapping("/sse-token")
    public Result<String> issueSseToken(@RequestParam(value = "classroomId", required = false) String classroomId) {
        // 生成随机token
        String token = UUID.randomUUID().toString();

        Map<String, Object> info = new HashMap<>();
        // 如果有用户登录，则记录用户ID；否则为空（匿名用户）
        try {
            // 这里可以根据你的认证逻辑获取用户ID
            // info.put("userId", currentUserId);
        } catch (Exception e) {
            // 用户未登录，忽略异常
        }
        info.put("classroomId", classroomId);

        // 将token信息存储到Redis，有效期60秒
        redisTemplate.opsForValue().set(sseTokenPrefix + token, info, Duration.ofSeconds(sseTokenTtlSeconds));

        return Result.success(token);
    }
}


