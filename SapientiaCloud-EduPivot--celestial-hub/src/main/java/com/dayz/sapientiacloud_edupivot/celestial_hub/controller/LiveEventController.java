package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.event.LiveEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/live")
public class LiveEventController {

    private final LiveEventPublisher liveEventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${live.sse.token.redis.prefix:sse:token:}")
    private String sseTokenPrefix;

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
}


