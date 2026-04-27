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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * Kafka bridge for AI question generation requests and responses.
 */
@Slf4j
@Service
public class KafkaQuestionService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Cache<String, Sinks.One<String>> responseSinks;
    private final Cache<String, Sinks.Many<QuestionProgressMessage>> progressSinks;

    @Value("${spring.kafka.topic.question-request:question-request-topic}")
    private String questionRequestTopic;

    @Value("${spring.kafka.topic.question-response:question-response-topic}")
    private String questionResponseTopic;

    @Value("${spring.kafka.topic.question-progress:question-progress-topic}")
    private String questionProgressTopic;

    @Value("${kafka.question.timeout-seconds:1800}")
    private long questionTimeoutSeconds;

    public KafkaQuestionService(KafkaTemplate<String, String> kafkaTemplate,
                                @Value("${kafka.question.response-cache-minutes:40}") long questionResponseCacheMinutes,
                                @Value("${kafka.question.progress-cache-minutes:20}") long questionProgressCacheMinutes) {
        this.kafkaTemplate = kafkaTemplate;
        this.responseSinks = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(questionResponseCacheMinutes, TimeUnit.MINUTES)
                .build();
        this.progressSinks = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(questionProgressCacheMinutes, TimeUnit.MINUTES)
                .build();
    }

    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request, String requestId) {
        return generateQuestions(request, requestId, null);
    }

    public List<QuestionResponseDTO> generateQuestions(QuestionGenerateRequestDTO request,
                                                       String requestId,
                                                       UUID currentUserId) {
        if (request == null) {
            throw new BusinessException(ResultEnum.PARAM_ERROR);
        }
        String finalRequestId = StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();

        UUID finalUserId = currentUserId != null ? currentUserId : UserContextUtil.getCurrentUserId();
        if (finalUserId == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED);
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
        QuestionResponseMessage responseMessage;
        try {
            responseMessage = JSON.parseObject(message, QuestionResponseMessage.class);
        } catch (Exception e) {
            log.debug("Failed to parse question response Kafka message. requestId={}", requestId, e);
            return;
        }
        if (responseMessage == null) {
            return;
        }
        String finalRequestId = StringUtils.hasText(responseMessage.getRequestId())
                ? responseMessage.getRequestId() : requestId;
        if (!StringUtils.hasText(finalRequestId)) {
            return;
        }

        Sinks.One<String> sink = responseSinks.getIfPresent(finalRequestId);
        if (sink == null) {
            return;
        }

        sink.tryEmitValue(responseMessage.getContent());
        responseSinks.invalidate(finalRequestId);
        completeQuestionProgress(finalRequestId);
    }

    public void sendQuestionResponse(String requestId, String jsonContent) {
        if (!StringUtils.hasText(requestId)) {
            return;
        }
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

    @KafkaListener(topics = "${spring.kafka.topic.question-progress:question-progress-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-question-progress")
    public void consumeQuestionProgress(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_KEY) String requestId) {
        QuestionProgressMessage progressMessage;
        try {
            progressMessage = JSON.parseObject(message, QuestionProgressMessage.class);
        } catch (Exception e) {
            log.debug("Failed to parse question progress Kafka message. requestId={}", requestId, e);
            return;
        }
        if (progressMessage == null) {
            return;
        }

        String finalRequestId = StringUtils.hasText(progressMessage.getRequestId())
                ? progressMessage.getRequestId() : requestId;
        if (!StringUtils.hasText(finalRequestId)) {
            return;
        }

        emitQuestionProgress(finalRequestId, progressMessage);
    }

    public void sendQuestionProgress(String requestId,
                                     UUID sessionId,
                                     String status,
                                     String stage,
                                     String message,
                                     Integer questionCount,
                                     String generationMode) {
        if (!StringUtils.hasText(requestId)) {
            return;
        }

        QuestionProgressMessage progressMessage = new QuestionProgressMessage();
        progressMessage.setRequestId(requestId);
        progressMessage.setSessionId(sessionId);
        progressMessage.setStatus(status);
        progressMessage.setStage(stage);
        progressMessage.setMessage(message);
        progressMessage.setQuestionCount(questionCount);
        progressMessage.setGenerationMode(generationMode);
        progressMessage.setTimestamp(System.currentTimeMillis());

        try {
            String payload = JSON.toJSONString(progressMessage);
            kafkaTemplate.send(questionProgressTopic, requestId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.debug("Failed to send question progress Kafka message. requestId={}", requestId, ex);
                        }
                    });
        } catch (Exception ex) {
            log.debug("Failed to serialize question progress Kafka message. requestId={}", requestId, ex);
        }
    }

    public Flux<QuestionProgressMessage> subscribeQuestionProgress(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return Flux.empty();
        }
        return getOrCreateQuestionProgressSink(requestId).asFlux();
    }

    public void clearQuestionProgress(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            return;
        }
        completeQuestionProgress(requestId);
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

    private Sinks.Many<QuestionProgressMessage> getOrCreateQuestionProgressSink(String requestId) {
        Sinks.Many<QuestionProgressMessage> sink = progressSinks.getIfPresent(requestId);
        if (sink != null) {
            return sink;
        }

        Sinks.Many<QuestionProgressMessage> created = Sinks.many().replay().limit(32);
        progressSinks.put(requestId, created);
        return created;
    }

    private void emitQuestionProgress(String requestId, QuestionProgressMessage progressMessage) {
        Sinks.Many<QuestionProgressMessage> sink = progressSinks.getIfPresent(requestId);
        if (sink == null) {
            return;
        }

        QuestionProgressMessage normalized = normalizeQuestionProgressMessage(requestId, progressMessage);
        sink.tryEmitNext(normalized);
    }

    private QuestionProgressMessage normalizeQuestionProgressMessage(String requestId,
                                                                    QuestionProgressMessage progressMessage) {
        if (progressMessage == null) {
            QuestionProgressMessage empty = new QuestionProgressMessage();
            empty.setRequestId(requestId);
            empty.setTimestamp(System.currentTimeMillis());
            return empty;
        }
        if (!StringUtils.hasText(progressMessage.getRequestId())) {
            progressMessage.setRequestId(requestId);
        }
        if (progressMessage.getTimestamp() == null) {
            progressMessage.setTimestamp(System.currentTimeMillis());
        }
        return progressMessage;
    }

    private void completeQuestionProgress(String requestId) {
        Sinks.Many<QuestionProgressMessage> sink = progressSinks.getIfPresent(requestId);
        if (sink != null) {
            sink.tryEmitComplete();
        }
        progressSinks.invalidate(requestId);
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

    @Getter
    @Setter
    public static class QuestionProgressMessage {
        private String requestId;
        private UUID sessionId;
        private String status;
        private String stage;
        private String message;
        private Integer questionCount;
        private String generationMode;
        private Long timestamp;
    }
}
