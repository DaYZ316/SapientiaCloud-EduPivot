package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dayz.sapientiacloud_edupivot.live.common.config.LiveKitProperties;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartRequest;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartResponse;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStopRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class LiveKitEgressClient {

    // LiveKit Egress HTTP/Twirp endpoints
    private static final String START_PATH = "/twirp/livekit.Egress/StartRoomCompositeEgress";
    private static final String STOP_PATH = "/twirp/livekit.Egress/StopEgress";

    private final RestTemplate restTemplate;
    private final LiveKitProperties liveKitProperties;

    public LiveKitEgressClient(LiveKitProperties liveKitProperties, RestTemplateBuilder restTemplateBuilder) {
        this.liveKitProperties = liveKitProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    public LiveKitEgressStartResponse startCompositeEgress(LiveKitEgressStartRequest request) {
        String endpoint = buildEndpoint(START_PATH);
        LiveKitEgressStartResponse response = restTemplate.postForObject(endpoint, buildEntity(request), LiveKitEgressStartResponse.class);
        if (response == null || !StringUtils.hasText(response.getEgressId())) {
            throw new BusinessException("LiveKit 返回为空，启动录制失败");
        }
        return response;
    }

    public void stopEgress(LiveKitEgressStopRequest request) {
        String endpoint = buildEndpoint(STOP_PATH);
        restTemplate.postForEntity(endpoint, buildEntity(request), Void.class);
    }

    private String buildEndpoint(String path) {
        String host = liveKitProperties.getHost();
        if (!StringUtils.hasText(host)) {
            throw new BusinessException("LiveKit host 未配置");
        }

        // Egress API 使用 HTTP(S)，若配置为 ws/wss，需转换
        if (host.startsWith("ws://")) {
            host = host.replaceFirst("ws://", "http://");
        } else if (host.startsWith("wss://")) {
            host = host.replaceFirst("wss://", "https://");
        }

        return UriComponentsBuilder.fromHttpUrl(host)
                .path(path)
                .toUriString();
    }

    private <T> HttpEntity<T> buildEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(buildEgressJwt());
        return new HttpEntity<>(body, headers);
    }

    /**
     * 构造 Egress API 所需的 JWT（包含 roomRecord 权限）
     */
    private String buildEgressJwt() {
        String apiKey = liveKitProperties.getApiKey();
        String apiSecret = liveKitProperties.getApiSecret();
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(apiSecret)) {
            throw new BusinessException("LiveKit API Key/Secret 未配置");
        }

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);

        return JWT.create()
                .withIssuer(apiKey)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(java.util.Date.from(now))
                .withExpiresAt(java.util.Date.from(exp))
                .withClaim("video", Map.of("roomRecord", true))
                .sign(Algorithm.HMAC256(apiSecret));
    }
}

