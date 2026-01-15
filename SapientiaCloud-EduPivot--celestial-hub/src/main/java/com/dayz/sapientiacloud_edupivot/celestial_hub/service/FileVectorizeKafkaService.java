package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileVectorizeRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 文件向量化Kafka生产者服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileVectorizeKafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.file-vectorize:file-vectorize-topic}")
    private String fileVectorizeTopic;

    /**
     * 发送文件向量化任务到Kafka
     *
     * @param request 文件向量化请求
     */
    public void sendVectorizeTask(FileVectorizeRequestDTO request) {
        try {
            String messageJson = JSON.toJSONString(request);
            String key = request.getFileId().toString();

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    fileVectorizeTopic, key, messageJson);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送文件向量化任务到Kafka失败, fileId: {}, error: {}",
                            request.getFileId(), ex.getMessage(), ex);
                } else {
                    log.debug("文件向量化任务已发送到Kafka, fileId: {}, topic: {}, partition: {}, offset: {}",
                            request.getFileId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("发送文件向量化任务异常, fileId: {}, error: {}",
                    request.getFileId(), e.getMessage(), e);
            throw e;
        }
    }
}

