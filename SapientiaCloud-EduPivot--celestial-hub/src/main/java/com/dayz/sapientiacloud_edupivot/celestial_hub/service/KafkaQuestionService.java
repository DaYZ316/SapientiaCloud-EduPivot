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
import java.util.concurrent.TimeoutException;

/**
 * Kafka bridge for AI question generation requests and responses.
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

    @Value("${kafka.question.timeout-seconds:1800}")
    private long questionTimeoutSeconds;

    public KafkaQuestionService(KafkaTemplate<String, String> kafkaTemplate,
                                @Value("${kafka.question.response-cache-minutes:40}")
                                long questionResponseCacheMinutes) {
        this.kafkaTemplate = kafkaTemplate;
        this.responseSinks = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(questionResponseCacheMinutes, TimeUnit.MINUTES)
                .build();
    }

    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request, String requestId) {
        return generateQuestions(request, requestId, null);
    }

    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request,
                                                       String requestId,
                                                       UUID currentUserId) {
        String finalRequestId = requestId != null ? requestId : UUID.randomUUID().toString();

        UUID finalUserId = currentUserId != null ? currentUserId : UserContextUtil.getCurrentUserId();
        if (finalUserId == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED.getMessage());
        }

        QuestionRequestMessage requestMessage = new QuestionRequestMessage();
        requestMessage.setRequestId(finalRequestId);
        requestMessage.setRequest(request);
        requestMessage.setUserId(finalUserId);

        Sinks.One<String> sink = responseSinks.getIfPresent(finalRequestId);
        boolean needDispatch = sink == null;
        if (needDispatch) {
            sink = Sinks.one();
            responseSinks.put(finalRequestId, sink);
        }

        try {
            if (needDispatch) {
                dispatchQuestionRequest(finalRequestId, requestMessage, sink);
            } else {
                log.debug("Reusing pending question request sink. requestId={}", finalRequestId);
            }

            String json = awaitQuestionResponse(finalRequestId, sink);
            if (!StringUtils.hasText(json)) {
                return List.of();
            }
            return JSON.parseArray(json, QuestionResponseDTO.class);
        } catch (Exception e) {
            log.debug("Failed to handle Kafka question request. requestId={}", finalRequestId, e);
            responseSinks.invalidate(finalRequestId);
            throw new RuntimeException("AI出题请求失败", e);
        }
    }

    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request) {
        return generateQuestions(request, null);
    }

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

    public void sendQuestionResponse(String requestId, String jsonContent) {
        QuestionResponseMessage response = new QuestionResponseMessage();
        response.setRequestId(requestId);
        response.setContent(jsonContent);
        try {
            String payload = JSON.toJSONString(response);
            kafkaTemplate.send(questionResponseTopic, requestId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.debug("Failed to send question response Kafka message. requestId={}", requestId, ex);
                        }
                    });
        } catch (Exception ex) {
            log.debug("Failed to serialize question response Kafka message. requestId={}", requestId, ex);
        }
    }

    public boolean checkRequestStatus(String requestId) {
        return responseSinks.getIfPresent(requestId) == null;
    }

    private void dispatchQuestionRequest(String requestId,
                                         QuestionRequestMessage requestMessage,
                                         Sinks.One<String> sink) {
        String payload = JSON.toJSONString(requestMessage);
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(questionRequestTopic, requestId, payload);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.debug("Failed to send question request Kafka message. requestId={}", requestId, ex);
                sink.tryEmitError(ex);
                responseSinks.invalidate(requestId);
            }
        });
    }

    private String awaitQuestionResponse(String requestId, Sinks.One<String> sink) {
        return sink.asMono()
                .timeout(Duration.ofSeconds(questionTimeoutSeconds))
                .onErrorResume(TimeoutException.class, ex -> {
                    log.warn("Question Kafka response wait timed out. requestId={}, timeoutSeconds={}",
                            requestId, questionTimeoutSeconds);
                    return Mono.empty();
                })
                .block();
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
        private String content;
    }
}
