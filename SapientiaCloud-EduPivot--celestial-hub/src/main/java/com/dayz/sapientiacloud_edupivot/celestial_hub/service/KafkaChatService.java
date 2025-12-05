package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
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
import java.util.concurrent.TimeoutException;

/**
 * Kafka聊天服务
 * 用于通过Kafka转发消息到Spring AI
 */
@Slf4j
@Service
public class KafkaChatService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    // 存储请求ID和对应的响应Sink，使用Caffeine缓存自动过期清理
    private final Cache<String, Sinks.Many<String>> responseSinks;
    // 存储请求ID和对应的请求对象，用于超时时保存系统消息
    private final Cache<String, KafkaChatRequestDTO> requestCache;
    @Value("${spring.kafka.topic.chat-request:chat-request-topic}")
    private String chatRequestTopic;
    @Value("${spring.kafka.topic.chat-response:chat-response-topic}")
    private String chatResponseTopic;
    @Value("${spring.kafka.topic.chat-control:chat-control-topic}")
    private String chatControlTopic;
    @Value("${kafka.chat.timeout-seconds:300}")
    private long chatTimeoutSeconds;

    public KafkaChatService(KafkaTemplate<String, String> kafkaTemplate,
                            ChatMessageRepository chatMessageRepository,
                            IChatSessionService chatSessionService) {
        this.kafkaTemplate = kafkaTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionService = chatSessionService;
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
        // 初始化请求缓存，用于超时时保存系统消息
        this.requestCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    /**
     * 通过Kafka发送聊天请求并返回流式响应
     */
    public Flux<String> chatStreamKafka(KafkaChatRequestDTO request) {
        long startTime = System.currentTimeMillis();
        // 生成或复用请求ID
        String requestId = StringUtils.hasText(request.getRequestId())
                ? request.getRequestId()
                : UUID.randomUUID().toString();
        request.setRequestId(requestId);

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

        // 缓存请求对象，用于超时时保存系统消息
        requestCache.put(requestId, request);

        // 检查会话是否有消息，如果没有则标记需要生成标题
        try {
            ChatSessionVO sessionVO = getOrCreateSession(request);
            UUID sessionId = sessionVO.getId();
            long messageCount = chatMessageRepository.countBySessionId(sessionId);
            // 如果会话中没有消息，标记需要生成标题（在响应完成后）
            if (messageCount == 0) {
                request.setNeedGenerateTitle(true);
            }
        } catch (Exception e) {
            log.warn("检查会话消息数量失败，跳过标题生成标记: requestId={}, error={}", requestId, e.getMessage());
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
                        log.debug("SSE流被取消或断开, requestId: {}, 仅清理本地资源", requestId);
                        // 客户端断开时，不立即移除sink，让kafka consumer继续处理
                        // responseSinks.invalidate(requestId);
                        // requestCache.invalidate(requestId);
                    })
                    .doOnComplete(() -> {
                        log.debug("响应流完成, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime);
                        responseSinks.invalidate(requestId);
                        requestCache.invalidate(requestId);
                    })
                    .doOnError(error -> {
                        if (error instanceof TimeoutException) {
                            // 超时时不立即移除sink，让kafka consumer继续处理
                            // 不调用notifyCancellation，让kafka consumer继续执行
                            log.warn("响应流超时，但继续执行kafka逻辑, requestId: {}, 耗时: {}ms",
                                    requestId, System.currentTimeMillis() - startTime);
                            // 超时时保存系统消息（但不阻止kafka继续处理）
                            saveTimeoutSystemMessage(requestId);
                            // 不立即移除sink，让kafka consumer继续发送数据
                        } else {
                            // 非超时错误，记录日志但不立即清理（让kafka consumer处理）
                            log.error("响应流错误, requestId: {}, 耗时: {}ms, error: {}",
                                    requestId, System.currentTimeMillis() - startTime, error.getMessage());
                        }
                    })
                    .doOnTerminate(() -> {
                        // 只有在真正完成时才清理资源
                        // 超时时不在这里清理，让kafka consumer继续处理
                        log.debug("响应流终止, requestId: {}, 耗时: {}ms", requestId,
                                System.currentTimeMillis() - startTime);
                    })
                    // 超时后继续接收数据，不立即终止流
                    .onErrorResume(TimeoutException.class, e -> {
                        log.info("响应流超时，但继续等待kafka响应, requestId: {}", requestId);
                        // 超时后继续等待kafka consumer发送数据，不立即终止
                        // 返回一个空的Flux，但sink仍然保留，kafka consumer可以继续发送
                        return Flux.empty();
                    });

        } catch (Exception e) {
            log.error("处理Kafka聊天请求失败, requestId: {}, 耗时: {}ms", requestId,
                    System.currentTimeMillis() - startTime, e);
            responseSink.tryEmitError(e);
            responseSinks.invalidate(requestId);
            requestCache.invalidate(requestId);
            return Flux.error(e);
        }
    }

    /**
     * 从缓存中获取请求对象
     */
    public KafkaChatRequestDTO getRequestFromCache(String requestId) {
        return requestCache.getIfPresent(requestId);
    }

    /**
     * 超时时保存系统消息
     */
    private void saveTimeoutSystemMessage(String requestId) {
        try {
            KafkaChatRequestDTO request = requestCache.getIfPresent(requestId);
            if (request == null) {
                log.warn("超时时无法获取请求信息, requestId: {}", requestId);
                return;
            }

            // 获取或创建会话
            ChatSessionVO sessionVO = getOrCreateSession(request);
            UUID sessionId = sessionVO.getId();

            // 保存系统消息
            String timeoutMessage = "对话请求超时，请稍后重试。";
            ChatMessageUtil.saveSystemMessage(sessionId, timeoutMessage, requestId, chatMessageRepository);
            log.info("超时系统消息已保存, requestId: {}, sessionId: {}", requestId, sessionId);
        } catch (Exception e) {
            log.error("保存超时系统消息失败, requestId: {}", requestId, e);
        }
    }

    /**
     * 获取或创建会话
     */
    private ChatSessionVO getOrCreateSession(KafkaChatRequestDTO request) {
        if (request.getSessionId() != null) {
            return chatSessionService.getChatSessionById(request.getSessionId());
        }
        UUID userId = request.getUserId();
        if (userId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }
        return chatSessionService.addChatSession(userId, request.getCourseId(), request.getSessionType(), null);
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
            // 如果sink已经终止（客户端断开），记录日志但不清理，让kafka consumer继续处理
            if (result == Sinks.EmitResult.FAIL_TERMINATED) {
                log.debug("客户端已断开，但继续处理kafka响应, requestId: {}", requestId);
                // 不清理sink，让kafka consumer继续发送数据
            } else if (result == Sinks.EmitResult.FAIL_OVERFLOW) {
                log.warn("响应sink溢出, requestId: {}, result: {}", requestId, result);
                // 溢出时也不清理，让kafka consumer继续处理
            } else {
                log.warn("发送响应chunk失败, requestId: {}, result: {}", requestId, result);
            }
        }
    }

    private void emitComplete(String requestId, Sinks.Many<String> responseSink) {
        responseSink.tryEmitComplete();
        // 清理资源
        responseSinks.invalidate(requestId);
        requestCache.invalidate(requestId);
        log.debug("Kafka响应完成，已清理资源, requestId: {}", requestId);
    }

    private void emitError(String requestId, Sinks.Many<String> responseSink, String errorMessage) {
        responseSink.tryEmitError(new RuntimeException(errorMessage));
        // 清理资源
        responseSinks.invalidate(requestId);
        requestCache.invalidate(requestId);
        log.debug("Kafka响应错误，已清理资源, requestId: {}, error: {}", requestId, errorMessage);
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

    /**
     * 取消Kafka聊天请求并立即清理资源（通过接口调用时使用）
     *
     * @param requestId 请求ID
     * @param reason    取消原因
     */
    public void cancelAndCleanup(String requestId, String reason) {
        if (!StringUtils.hasText(requestId)) {
            log.warn("取消请求时requestId为空");
            return;
        }

        // 1. 通知Worker终止处理
        notifyCancellation(requestId, StringUtils.hasText(reason) ? reason : "manual_cancel");

        // 2. 立即清理本地资源
        Sinks.Many<String> responseSink = responseSinks.getIfPresent(requestId);
        if (responseSink != null) {
            // 尝试完成sink（如果还有订阅者）
            responseSink.tryEmitComplete();
            // 移除sink
            responseSinks.invalidate(requestId);
            log.debug("已清理响应sink, requestId: {}", requestId);
        }

        // 3. 清理请求缓存
        requestCache.invalidate(requestId);
        log.info("已取消Kafka聊天请求并清理资源, requestId: {}, reason: {}", requestId, reason);
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

    public enum ChatResponseType {
        CHUNK, COMPLETE, ERROR
    }

    public enum ChatControlType {
        CANCEL
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
}
