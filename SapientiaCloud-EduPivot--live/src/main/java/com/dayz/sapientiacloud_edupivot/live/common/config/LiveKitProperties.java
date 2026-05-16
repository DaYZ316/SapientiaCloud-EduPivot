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
        /**
         * LiveKit 录制布局
         */
        private String layout = "speaker-dark";
        /**
         * 文件输出类型，默认 MP4
         */
        private String fileType = "MP4";
        /**
         * 输出相对路径前缀
         */
        private String outputPrefix = "live-playback";
        /**
         * 录制回放访问前缀
         */
        private String playbackBaseUrl;
        /**
         * S3/MinIO 上传配置
         */
        private S3Config s3;
    }

    @Data
    public static class S3Config {
        private String endpoint;
        private String region;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private Boolean forcePathStyle;
    }
}