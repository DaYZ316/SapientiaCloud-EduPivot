package com.dayz.sapientiacloud_edupivot.classroom.websocket;

import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSyncWebSocketHub {

    private final ObjectMapper objectMapper;
    private final Map<UUID, Set<WebSocketSession>> sessionsByRecord = new ConcurrentHashMap<>();

    public void addSession(UUID recordId, WebSocketSession session) {
        sessionsByRecord.computeIfAbsent(recordId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(UUID recordId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByRecord.get(recordId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByRecord.remove(recordId);
        }
    }

    public void sendSnapshotToSession(WebSocketSession session, UUID recordId, List<CourseRecordStudentVO> seats) {
        Map<String, Object> data = new HashMap<>();
        data.put("seats", seats == null ? List.of() : seats);
        Map<String, Object> payload = buildPayload("seat_snapshot", recordId, data);
        sendMessage(session, payload);
    }

    public void publishSeatUpsert(UUID recordId, CourseRecordStudentVO seat) {
        if (seat == null) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("seat", seat);
        Map<String, Object> payload = buildPayload("seat_upsert", recordId, data);
        broadcast(recordId, payload);
    }

    public void publishSeatRemove(UUID recordId, UUID studentId, Integer seatIndex) {
        Map<String, Object> data = new HashMap<>();
        data.put("studentId", studentId == null ? null : studentId.toString());
        data.put("seatIndex", seatIndex);
        Map<String, Object> payload = buildPayload("seat_remove", recordId, data);
        broadcast(recordId, payload);
    }

    private void broadcast(UUID recordId, Map<String, Object> payload) {
        Set<WebSocketSession> sessions = sessionsByRecord.get(recordId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        List<WebSocketSession> disconnected = new ArrayList<>();
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                disconnected.add(session);
                continue;
            }
            sendMessage(session, payload);
        }

        if (!disconnected.isEmpty()) {
            disconnected.forEach(session -> removeSession(recordId, session));
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            log.warn("Failed to push seat sync message to session {}: {}", session.getId(), e.getMessage());
            detachSessionFromAllRooms(session);
        }
    }

    private void detachSessionFromAllRooms(WebSocketSession session) {
        Set<UUID> roomIds = new HashSet<>(sessionsByRecord.keySet());
        for (UUID roomId : roomIds) {
            removeSession(roomId, session);
        }
    }

    private Map<String, Object> buildPayload(String event, UUID recordId, Object data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("recordId", recordId.toString());
        payload.put("version", System.currentTimeMillis());
        payload.put("serverTime", LocalDateTime.now().toString());
        payload.put("data", data);
        return payload;
    }
}
