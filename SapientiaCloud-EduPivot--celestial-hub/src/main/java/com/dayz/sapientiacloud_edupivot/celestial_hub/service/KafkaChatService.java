package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
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
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Kafka聊天服务
 * 用于通过Kafka转发消息到Spring AI
 */
@Slf4j
@Service
public class KafkaChatService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    // 存储请求ID和对应的响应Sink，使用Caffeine缓存自动过期清理
    private final Cache<String, Sinks.Many<String>> responseSinks;
    @Value("${spring.kafka.topic.chat-request:chat-request-topic}")
    private String chatRequestTopic;
    @Value("${spring.kafka.topic.chat-response:chat-response-topic}")
    private String chatResponseTopic;
    @Value("${spring.kafka.topic.chat-control:chat-control-topic}")
    private String chatControlTopic;
    @Value("${kafka.chat.timeout-seconds:300}")
    private long chatTimeoutSeconds;

    public KafkaChatService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        // 初始化Caffeine缓存，自动清理过期项
        this.responseSinks = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .removalListener((key, value, cause) -> {
                    @SuppressWarnings("unchecked")
                    Sinks.Many<String> sink = (Sinks.Many<String>) value;
                    if (sink != null && sink.currentSubscriberCount() != 0) {
                        log.debug("响应Sink被移除但仍有订阅者, requestId: {}, cause: {}", key, cause);
                    }
                    log.debug("响应Sink被移除, requestId: {}, cause: {}", key, cause);
                })
                .recordStats()
                .build();
    }

    /**
     * 通过Kafka发送聊天请求并返回流式响应
     */
    public Flux<String> chatStreamKafka(KafkaChatRequestDTO request) {
        long startTime = System.currentTimeMillis();
        // 生成请求ID
        String requestId = UUID.randomUUID().toString();

        // 创建响应Sink
        Sinks.Many<String> responseSink = Sinks.many().unicast().onBackpressureBuffer();
        responseSinks.put(requestId, responseSink);

        // 如果请求中没有userId，从当前上下文获取
        if (request.getUserId() == null) {
            try {
                request.setUserId(UserContextUtil.getCurrentUserId());
            } catch (Exception e) {
                log.warn("无法从上下文获取用户ID: {}", e.getMessage());
            }
        }

        // 创建请求消息，包含请求ID
        ChatRequestMessage requestMessage = new ChatRequestMessage();
        requestMessage.setRequestId(requestId);
        requestMessage.setRequest(request);

        try {
            // 发送消息到Kafka
            String messageJson = JSON.toJSONString(requestMessage);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(chatRequestTopic, requestId, messageJson);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("发送Kafka消息失败, requestId: {}, 耗时: {}ms", requestId,
                            System.currentTimeMillis() - startTime, ex);
                    responseSink.tryEmitError(ex);
                    responseSinks.invalidate(requestId);
                }
            });

            // 返回响应流，使用配置的超时时间
            return responseSink.asFlux()
                    .timeout(Duration.ofSeconds(chatTimeoutSeconds))
                    .doOnCancel(() -> {
                        notifyCancellation(requestId, "client_cancelled");
                        responseSinks.invalidate(requestId);
                    })
                    .doOnComplete(() -> responseSinks.invalidate(requestId))
                    .doOnError(error -> {
                        log.error("响应流错误, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime, error);
                        if (error instanceof java.util.concurrent.TimeoutException) {
                            notifyCancellation(requestId, "stream_timeout");
                        }
                        responseSinks.invalidate(requestId);
                    })
                    .doOnTerminate(() -> responseSinks.invalidate(requestId));

        } catch (Exception e) {
            log.error("处理Kafka聊天请求失败, requestId: {}, 耗时: {}ms", requestId,
                    System.currentTimeMillis() - startTime, e);
            responseSink.tryEmitError(e);
            responseSinks.invalidate(requestId);
            return Flux.error(e);
        }
    }

    /**
     * 处理来自Kafka的响应消息（网关侧消费响应，并写入Sink）
     */
    @KafkaListener(topics = "${spring.kafka.topic.chat-response:chat-response-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-response")
    public void consumeChatResponse(@Payload String message,
                                    @Header(KafkaHeaders.RECEIVED_KEY) String requestId) {
        ChatResponseMessage responseMessage = JSON.parseObject(message, ChatResponseMessage.class);
        String finalRequestId = StringUtils.hasText(responseMessage.getRequestId())
                ? responseMessage.getRequestId() : requestId;

        Sinks.Many<String> responseSink = responseSinks.getIfPresent(finalRequestId);
        if (responseSink == null) {
            return;
        }

        ChatResponseType type = responseMessage.getType();
        switch (type) {
            case CHUNK -> emitChunk(finalRequestId, responseSink, responseMessage.getContent());
            case COMPLETE -> emitComplete(finalRequestId, responseSink);
            case ERROR -> emitError(finalRequestId, responseSink, responseMessage.getError());
            default -> log.warn("未知的响应类型: {}, requestId: {}", type, finalRequestId);
        }
    }

    private void emitChunk(String requestId, Sinks.Many<String> responseSink, String chunk) {
        Sinks.EmitResult result = responseSink.tryEmitNext(chunk);
        if (result.isFailure()) {
            log.warn("发送响应chunk失败, requestId: {}, result: {}", requestId, result);
            if (result == Sinks.EmitResult.FAIL_TERMINATED || result == Sinks.EmitResult.FAIL_OVERFLOW) {
                responseSinks.invalidate(requestId);
            }
        }
    }

    private void emitComplete(String requestId, Sinks.Many<String> responseSink) {
        responseSink.tryEmitComplete();
        responseSinks.invalidate(requestId);
    }

    private void emitError(String requestId, Sinks.Many<String> responseSink, String errorMessage) {
        responseSink.tryEmitError(new RuntimeException(errorMessage));
        responseSinks.invalidate(requestId);
    }

    /**
     * Worker侧调用：发送响应chunk
     */
    public void handleResponse(String requestId, String response) {
        ChatResponseMessage message = new ChatResponseMessage();
        message.setRequestId(requestId);
        message.setType(ChatResponseType.CHUNK);
        message.setContent(response);
        sendResponseMessage(message);
    }

    /**
     * Worker侧调用：完成响应流
     */
    public void completeResponse(String requestId) {
        ChatResponseMessage message = new ChatResponseMessage();
        message.setRequestId(requestId);
        message.setType(ChatResponseType.COMPLETE);
        sendResponseMessage(message);
    }

    /**
     * Worker侧调用：响应错误
     */
    public void handleError(String requestId, Throwable error) {
        ChatResponseMessage message = new ChatResponseMessage();
        message.setRequestId(requestId);
        message.setType(ChatResponseType.ERROR);
        message.setError(error != null ? error.getMessage() : "unknown error");
        sendResponseMessage(message);
    }

    private void sendResponseMessage(ChatResponseMessage message) {
        try {
            String payload = JSON.toJSONString(message);
            kafkaTemplate.send(chatResponseTopic, message.getRequestId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送响应Kafka消息失败, requestId: {}", message.getRequestId(), ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("序列化响应Kafka消息失败, requestId: {}", message.getRequestId(), ex);
        }
    }

    /**
     * 客户端取消/超时，通知Worker终止处理
     */
    public void notifyCancellation(String requestId, String reason) {
        ChatControlMessage message = new ChatControlMessage();
        message.setRequestId(requestId);
        message.setType(ChatControlType.CANCEL);
        message.setReason(reason);
        sendControlMessage(message);
    }

    private void sendControlMessage(ChatControlMessage message) {
        try {
            String payload = JSON.toJSONString(message);
            kafkaTemplate.send(chatControlTopic, message.getRequestId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("发送控制Kafka消息失败, requestId: {}", message.getRequestId(), ex);
                        }
                    });
        } catch (Exception ex) {
            log.error("序列化控制Kafka消息失败, requestId: {}", message.getRequestId(), ex);
        }
    }

    /**
     * Kafka请求消息
     */
    @Setter
    @Getter
    public static class ChatRequestMessage {
        private String requestId;
        private KafkaChatRequestDTO request;
    }

    /**
     * Kafka响应消息
     */
    @Setter
    @Getter
    public static class ChatResponseMessage {
        private String requestId;
        private ChatResponseType type;
        private String content;
        private String error;
    }

    public enum ChatResponseType {
        CHUNK, COMPLETE, ERROR
    }

    /**
     * Kafka控制消息
     */
    @Setter
    @Getter
    public static class ChatControlMessage {
        private String requestId;
        private ChatControlType type;
        private String reason;
    }

    public enum ChatControlType {
        CANCEL
    }
}
