package com.dayz.sapientiacloud_edupivot.live.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "livekit")
public class LiveKitProperties {
    private String host;
    private String apiKey;
    private String apiSecret;
    private Integer tokenTtlSeconds;
    private RoomDefaults room;
    private EgressDefaults egress;

    @Data
    public static class RoomDefaults {
        private Integer emptyTimeoutSeconds;
        private Integer departureTimeoutSeconds;
        private Integer maxParticipants;
    }

    @Data
    public static class EgressDefaults {
        private Boolean enable;
    }
}