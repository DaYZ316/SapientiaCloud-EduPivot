package com.dayz.sapientiacloud_edupivot.celestial_hub.clients;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.LocalTtsRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.LocalTtsAudioVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class LocalTtsClient {

    private final RestClient restClient;
    private final String synthesizePath;

    public LocalTtsClient(@Value("${avatar.tts.worker-base-url:http://127.0.0.1:32111}") String workerBaseUrl,
                          @Value("${avatar.tts.worker-synthesize-path:/tts/synthesize}") String synthesizePath) {
        this.restClient = RestClient.builder()
                .baseUrl(workerBaseUrl)
                .build();
        this.synthesizePath = synthesizePath;
    }

    public LocalTtsAudioVO synthesize(LocalTtsRequestDTO request) {
        return restClient.post()
                .uri(synthesizePath)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(request)
                .exchange((clientRequest, clientResponse) -> {
                    if (!clientResponse.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("本地TTS服务调用失败，状态码: " + clientResponse.getStatusCode().value());
                    }
                    byte[] bytes = clientResponse.bodyTo(byte[].class);
                    if (bytes == null || bytes.length == 0) {
                        throw new IllegalStateException("本地TTS服务未返回音频数据");
                    }
                    String durationHeader = clientResponse.getHeaders().getFirst("X-Audio-Duration-Ms");
                    Integer durationMs = parseDuration(durationHeader);
                    return new LocalTtsAudioVO(bytes, durationMs);
                });
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
