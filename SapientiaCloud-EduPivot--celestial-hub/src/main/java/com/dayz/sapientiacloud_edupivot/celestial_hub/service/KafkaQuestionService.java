package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * AI出题 Kafka 服务
 * 通过 Kafka 转发出题请求并等待结果
 */
@Slf4j
@Service
public class KafkaQuestionService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Cache<String, Sinks.One<String>> responseSinks;

    @Value("${spring.kafka.topic.question-request:question-request-topic}")
    private String questionRequestTopic;
    @Value("${spring.kafka.topic.question-response:question-response-topic}")
    private String questionResponseTopic;
    @Value("${kafka.question.timeout-seconds:300}")
    private long questionTimeoutSeconds;

    public KafkaQuestionService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.responseSinks = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 发送出题请求，并同步等待完整结果（内部使用 Reactor 实现超时控制）
     * 
     * @param request   出题请求
     * @param requestId 请求ID；允许外部传入以便多次重试时复用同一个 requestId，若为 null 则自动生成
     */
    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request, String requestId) {
        String finalRequestId = (requestId != null ? requestId : UUID.randomUUID().toString());

        UUID currentUserId = UserContextUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED.getMessage());
        }

        QuestionRequestMessage requestMessage = new QuestionRequestMessage();
        requestMessage.setRequestId(finalRequestId);
        requestMessage.setRequest(request);
        requestMessage.setUserId(currentUserId);

        Sinks.One<String> sink = Sinks.one();
        responseSinks.put(finalRequestId, sink);

        try {
            String payload = JSON.toJSONString(requestMessage);
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(questionRequestTopic, finalRequestId, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送出题Kafka消息失败, requestId={}", finalRequestId, ex);
                    sink.tryEmitError(ex);
                    responseSinks.invalidate(finalRequestId);
                }
            });

            // 等待响应
            String json = Mono.fromCallable(() -> sink.asMono().block(Duration.ofSeconds(questionTimeoutSeconds)))
                    .timeout(Duration.ofSeconds(questionTimeoutSeconds))
                    .block();

            if (!StringUtils.hasText(json)) {
                return List.of();
            }
            return JSON.parseArray(json, QuestionResponseDTO.class);
        } catch (Exception e) {
            log.error("处理出题Kafka请求失败, requestId={}", finalRequestId, e);
            throw new RuntimeException("AI出题请求失败", e);
        } finally {
            responseSinks.invalidate(finalRequestId);
        }
    }

    /**
     * 兼容旧接口：不显式传入 requestId 时，每次调用都会生成新的 requestId
     */
    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request) {
        return generateQuestions(request, null);
    }

    /**
     * 网关侧消费出题结果
     */
    @KafkaListener(topics = "${spring.kafka.topic.question-response:question-response-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-question-response")
    public void consumeQuestionResponse(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_KEY) String requestId) {
        QuestionResponseMessage responseMessage = JSON.parseObject(message, QuestionResponseMessage.class);
        String finalRequestId = StringUtils.hasText(responseMessage.getRequestId())
                ? responseMessage.getRequestId() : requestId;

        Sinks.One<String> sink = responseSinks.getIfPresent(finalRequestId);
        if (sink == null) {
            return;
        }
        sink.tryEmitValue(responseMessage.getContent());
        responseSinks.invalidate(finalRequestId);
    }

    /**
     * Worker 侧调用：发送出题结果
     */
    public void sendQuestionResponse(String requestId, String jsonContent) {
        QuestionResponseMessage response = new QuestionResponseMessage();
        response.setRequestId(requestId);
        response.setContent(jsonContent);
        try {
            String payload = JSON.toJSONString(response);
            kafkaTemplate.send(questionResponseTopic, requestId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送出题响应Kafka消息失败, requestId={}", requestId, ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("序列化出题响应Kafka消息失败, requestId={}", requestId, ex);
        }
    }

    /**
     * 检查 Kafka 请求状态
     * 
     * @param requestId 请求ID
     * @return true：已完成（或未找到），false：进行中
     */
    public boolean checkRequestStatus(String requestId) {
        // 如果 responseSinks 中还存在该 requestId，说明请求还在进行中
        // 如果不存在，说明已完成（或未找到）
        return responseSinks.getIfPresent(requestId) == null;
    }

    @Getter
    @Setter
    public static class QuestionRequestMessage {
        private String requestId;
        private QuestionGenerateRequestDTO request;
        private UUID userId;
    }

    @Getter
    @Setter
    public static class QuestionResponseMessage {
        private String requestId;
        /**
         * 出题结果 JSON 数组字符串（List<QuestionResponseDTO>）
         */
        private String content;
    }
}



