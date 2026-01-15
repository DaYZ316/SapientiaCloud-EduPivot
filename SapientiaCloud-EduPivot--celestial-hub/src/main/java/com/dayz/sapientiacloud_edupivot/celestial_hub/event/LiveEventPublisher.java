package com.dayz.sapientiacloud_edupivot.celestial_hub.event;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE 发布器 - 用于将 Kafka 转发来的直播事件分发给前端订阅者
 */
@Component
public class LiveEventPublisher {

    // key: classroomId string, value: list of emitters
    private final Map<String, List<SseEmitter>> emittersByClassroom = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 0L;

    public SseEmitter subscribe(String classroomId) {
        String key = classroomId == null ? "global" : classroomId;
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emittersByClassroom.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key, emitter));
        emitter.onError((e) -> removeEmitter(key, emitter));

        return emitter;
    }

    public void publishToClassroom(String classroomId, Object payload) {
        String key = classroomId == null ? "global" : classroomId;
        publishToKey(key, payload);
        // also publish to global subscribers
        publishToKey("global", payload);
    }

    private void publishToKey(String key, Object payload) {
        List<SseEmitter> list = emittersByClassroom.get(key);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().data(payload));
            } catch (IOException e) {
                removeEmitter(key, emitter);
            }
        }
    }

    private void removeEmitter(String key, SseEmitter emitter) {
        List<SseEmitter> list = emittersByClassroom.get(key);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersByClassroom.remove(key);
            }
        }
    }
}


