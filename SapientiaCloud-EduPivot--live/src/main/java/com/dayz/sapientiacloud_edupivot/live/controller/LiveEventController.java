package com.dayz.sapientiacloud_edupivot.live.controller;

import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.event.LiveEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
        redisTemplate.delete(sseTokenPrefix + token);
        return liveEventPublisher.subscribe(classroomId);
    }

    /**
     * 生成 SSE token，用于直播事件订阅
     */
    @PostMapping("/sse-token")
    public Result<String> issueSseToken(@RequestParam(value = "classroomId", required = false) String classroomId) {
        String token = UUID.randomUUID().toString();

        Map<String, Object> info = new HashMap<>();
        info.put("classroomId", classroomId);

        redisTemplate.opsForValue().set(sseTokenPrefix + token, info, Duration.ofSeconds(sseTokenTtlSeconds));

        return Result.success(token);
    }
}
