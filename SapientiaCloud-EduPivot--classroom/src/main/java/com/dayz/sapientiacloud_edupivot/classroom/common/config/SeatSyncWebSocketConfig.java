package com.dayz.sapientiacloud_edupivot.classroom.common.config;

import com.dayz.sapientiacloud_edupivot.classroom.websocket.SeatSyncHandshakeInterceptor;
import com.dayz.sapientiacloud_edupivot.classroom.websocket.SeatSyncWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class SeatSyncWebSocketConfig implements WebSocketConfigurer {

    private final SeatSyncWebSocketHandler seatSyncWebSocketHandler;
    private final SeatSyncHandshakeInterceptor seatSyncHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(seatSyncWebSocketHandler, "/ws/seat")
                .addInterceptors(seatSyncHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
