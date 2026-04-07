package com.dayz.sapientiacloud_edupivot.classroom.websocket;

import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSyncWebSocketHandler extends TextWebSocketHandler {

    private final SeatSyncWebSocketHub seatSyncWebSocketHub;
    private final ICourseRecordStudentService courseRecordStudentService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID recordId = extractRecordId(session);
        if (recordId == null) {
            closeQuietly(session, CloseStatus.BAD_DATA);
            return;
        }

        seatSyncWebSocketHub.addSession(recordId, session);
        List<CourseRecordStudentVO> currentSeats = courseRecordStudentService.listStudentsByRecordId(recordId);
        seatSyncWebSocketHub.sendSnapshotToSession(session, recordId, currentSeats);
        log.debug("Seat sync connected, recordId={}, session={}", recordId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UUID recordId = extractRecordId(session);
        if (recordId != null) {
            seatSyncWebSocketHub.removeSession(recordId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        UUID recordId = extractRecordId(session);
        if (recordId != null) {
            seatSyncWebSocketHub.removeSession(recordId, session);
        }
        closeQuietly(session, CloseStatus.SERVER_ERROR);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("pong"));
                }
            } catch (Exception ignored) {
                closeQuietly(session, CloseStatus.SERVER_ERROR);
            }
        }
    }

    private UUID extractRecordId(WebSocketSession session) {
        Object rawRecordId = session.getAttributes().get("recordId");
        if (rawRecordId instanceof UUID uuid) {
            return uuid;
        }
        if (rawRecordId instanceof String value) {
            try {
                return UUID.fromString(value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (Exception e) {
            log.debug("Close websocket failed, session={}, err={}", session.getId(), e.getMessage());
        }
    }
}
