package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit;

import com.dayz.sapientiacloud_edupivot.live.common.config.LiveKitProperties;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartRequest;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartResponse;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStopRequest;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
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

@Slf4j
@Component
public class LiveKitEgressClient {

    private static final String START_PATH = "/egress/start-room-composite";
    private static final String STOP_PATH = "/egress/stop";

    private final RestTemplate restTemplate;
    private final LiveKitProperties liveKitProperties;

    public LiveKitEgressClient(LiveKitProperties liveKitProperties, RestTemplateBuilder restTemplateBuilder) {
        this.liveKitProperties = liveKitProperties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .basicAuthentication(liveKitProperties.getApiKey(), liveKitProperties.getApiSecret())
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
        return UriComponentsBuilder.fromHttpUrl(liveKitProperties.getHost())
                .path(path)
                .toUriString();
    }

    private <T> HttpEntity<T> buildEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}

