package com.dayz.sapientiacloud_edupivot.celestial_hub.clients;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.LocalTtsRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.LocalTtsAudioVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
public class LocalTtsClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String workerBaseUrl;
    private final String synthesizePath;

    public LocalTtsClient(@Value("${avatar.tts.worker-base-url:http://127.0.0.1:32111}") String workerBaseUrl,
                          @Value("${avatar.tts.worker-synthesize-path:/tts/synthesize}") String synthesizePath,
                          ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.objectMapper = objectMapper;
        this.workerBaseUrl = removeTrailingSlash(workerBaseUrl);
        this.synthesizePath = synthesizePath;
    }

    public LocalTtsAudioVO synthesize(LocalTtsRequestDTO request) {
        String requestBody = toJson(request);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(workerBaseUrl + normalizePath(synthesizePath)))
                .timeout(Duration.ofSeconds(65))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "audio/wav, application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String responseBody = response.body() == null ? "" : new String(response.body());
                String message = "本地TTS服务调用失败，状态码: " + response.statusCode();
                if (!responseBody.isBlank()) {
                    message += "，响应: " + responseBody;
                }
                throw new IllegalStateException(message);
            }
            byte[] bytes = response.body();
            if (bytes == null || bytes.length == 0) {
                throw new IllegalStateException("本地TTS服务未返回音频数据");
            }
            Integer durationMs = response.headers()
                    .firstValue("X-Audio-Duration-Ms")
                    .map(this::parseDuration)
                    .orElse(null);
            return new LocalTtsAudioVO(bytes, durationMs);
        } catch (IOException ex) {
            throw new IllegalStateException("本地TTS服务调用失败", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("本地TTS服务调用被中断", ex);
        }
    }

    private String toJson(LocalTtsRequestDTO request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("序列化本地TTS请求失败", ex);
        }
    }

    private String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private Integer parseDuration(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            log.debug("解析音频时长失败: {}", value);
            return null;
        }
    }
}
