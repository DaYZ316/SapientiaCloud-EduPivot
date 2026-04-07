package com.dayz.sapientiacloud_edupivot.classroom.controller;

import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.common.security.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/seat-sync")
@RequiredArgsConstructor
public class SeatSyncController {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${classroom.seat-sync.token.redis.prefix:classroom:seat-sync:token:}")
    private String wsTokenPrefix;

    @Value("${classroom.seat-sync.token.ttl.seconds:60}")
    private long wsTokenTtlSeconds;

    @PostMapping("/ws-token")
    public Result<String> issueWsToken(@RequestParam("recordId") UUID recordId) {
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        String token = UUID.randomUUID().toString();

        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("recordId", recordId.toString());
        tokenPayload.put("userId", currentUserId.toString());
        redisTemplate.opsForValue().set(
                wsTokenPrefix + token,
                tokenPayload,
                Duration.ofSeconds(wsTokenTtlSeconds)
        );

        return Result.success(token);
    }
}
