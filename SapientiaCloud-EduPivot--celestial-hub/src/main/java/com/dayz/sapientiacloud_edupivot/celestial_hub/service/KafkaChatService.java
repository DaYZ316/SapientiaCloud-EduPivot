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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
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
                    if (sink.currentSubscriberCount() != 0) {
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
                } else {
                    log.debug("Kafka消息发送成功, requestId: {}, topic: {}, partition: {}, offset: {}, 耗时: {}ms",
                            requestId, result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            System.currentTimeMillis() - startTime);
                }
            });

            // 返回响应流，使用配置的超时时间
            return responseSink.asFlux()
                    .timeout(Duration.ofSeconds(chatTimeoutSeconds))
                    .doOnCancel(() -> {
                        log.debug("客户端取消请求, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime);
                        responseSinks.invalidate(requestId);
                    })
                    .doOnComplete(() -> {
                        log.debug("响应流完成, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime);
                        responseSinks.invalidate(requestId);
                    })
                    .doOnError(error -> {
                        log.error("响应流错误, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime, error);
                        responseSinks.invalidate(requestId);
                    })
                    .doOnTerminate(() -> {
                        // 确保资源被清理
                        responseSinks.invalidate(requestId);
                        // 记录缓存统计信息
                        var stats = responseSinks.stats();
                        log.debug("缓存统计 - 大小: {}, 命中率: {}%, 移除数: {}",
                                responseSinks.estimatedSize(),
                                String.format("%.2f", stats.hitRate() * 100),
                                stats.evictionCount());
                    });

        } catch (Exception e) {
            log.error("处理Kafka聊天请求失败, requestId: {}, 耗时: {}ms", requestId,
                    System.currentTimeMillis() - startTime, e);
            responseSink.tryEmitError(e);
            responseSinks.invalidate(requestId);
            return Flux.error(e);
        }
    }

    /**
     * 处理来自Kafka的响应消息
     */
    public void handleResponse(String requestId, String response) {
        Sinks.Many<String> responseSink = responseSinks.getIfPresent(requestId);
        if (responseSink != null) {
            Sinks.EmitResult result = responseSink.tryEmitNext(response);
            if (result.isFailure()) {
                log.warn("发送响应chunk失败, requestId: {}, result: {}", requestId, result);
                if (result == Sinks.EmitResult.FAIL_TERMINATED || result == Sinks.EmitResult.FAIL_OVERFLOW) {
                    responseSinks.invalidate(requestId);
                }
            }
        } else {
            log.debug("未找到对应的请求ID: {}", requestId);
        }
    }

    /**
     * 完成响应流
     */
    public void completeResponse(String requestId) {
        Sinks.Many<String> responseSink = responseSinks.getIfPresent(requestId);
        if (responseSink != null) {
            responseSink.tryEmitComplete();
            responseSinks.invalidate(requestId);
        }
    }

    /**
     * 处理响应错误
     */
    public void handleError(String requestId, Throwable error) {
        Sinks.Many<String> responseSink = responseSinks.getIfPresent(requestId);
        if (responseSink != null) {
            responseSink.tryEmitError(error);
            responseSinks.invalidate(requestId);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        var stats = responseSinks.stats();
        return String.format("缓存统计 - 大小: %d, 命中数: %d, 未命中数: %d, 命中率: %.2f%%, 移除数: %d",
                responseSinks.estimatedSize(),
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100,
                stats.evictionCount());
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
}
