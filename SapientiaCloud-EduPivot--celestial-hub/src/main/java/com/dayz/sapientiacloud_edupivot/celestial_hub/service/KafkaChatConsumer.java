package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka聊天消息消费者
 * 从Kafka接收聊天请求，调用Spring AI处理，并将结果发送回Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaChatConsumer {

    /**
     * 请求记录保留时间（毫秒）
     */
    private static final long REQUEST_RETAIN_MS = 10 * 60 * 1000;
    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    private final KnowledgeService knowledgeService;
    private final KafkaChatService kafkaChatService;
    private final TtsAudioService ttsAudioService;
    /**
     * 已处理/正在处理的请求ID集合，防止Kafka重连时重复处理
     */
    private final ConcurrentHashMap<String, RequestRecord> processedRequests = new ConcurrentHashMap<>();
    /**
     * 客户端取消的请求集合
     */
    private final ConcurrentHashMap<String, Boolean> cancelledRequests = new ConcurrentHashMap<>();
    /**
     * 正在执行的流订阅集合
     */
    private final ConcurrentHashMap<String, Disposable> activeSubscriptions = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${spring.kafka.topic.chat-request:chat-request-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}")
    public void consumeChatRequest(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String requestId,
                                   Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        KafkaChatService.ChatRequestMessage requestMessage =
                JSON.parseObject(message, KafkaChatService.ChatRequestMessage.class);

        // 优先使用消息体中的requestId
        String finalRequestId = StringUtils.hasText(requestMessage.getRequestId())
                ? requestMessage.getRequestId() : requestId;

        try {
            // 幂等性检查
            if (!tryAcquireProcessingLock(finalRequestId)) {
                acknowledge(acknowledgment);
                return;
            }

            KafkaChatRequestDTO request = requestMessage.getRequest();
            if (request == null) {
                releaseProcessingLock(finalRequestId);
                throw new IllegalArgumentException("请求消息不能为空");
            }

            processChatRequest(finalRequestId, request, acknowledgment);

        } catch (Exception e) {
            releaseProcessingLock(finalRequestId);
            clearCancellationFlag(finalRequestId);
            kafkaChatService.handleError(finalRequestId, e);

            // 可重试错误不确认，等待重试
            if (acknowledgment != null && !ChatMessageUtil.isRetryableError(e)) {
                acknowledgment.acknowledge();
            }
        }
    }

    /**
     * 监听控制主题，处理取消指令
     */
    @KafkaListener(topics = "${spring.kafka.topic.chat-control:chat-control-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-control")
    public void consumeChatControl(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String requestId) {
        KafkaChatService.ChatControlMessage controlMessage =
                JSON.parseObject(message, KafkaChatService.ChatControlMessage.class);
        String finalRequestId = StringUtils.hasText(controlMessage.getRequestId())
                ? controlMessage.getRequestId() : requestId;
        if (controlMessage.getType() == KafkaChatService.ChatControlType.CANCEL) {
            boolean knownRequest = processedRequests.containsKey(finalRequestId);
            cancelledRequests.put(finalRequestId, true);
            Disposable disposable = activeSubscriptions.remove(finalRequestId);
            if (disposable != null && !disposable.isDisposed()) {
                log.debug("取消指令触发流式任务终止, requestId: {}", finalRequestId);
                disposable.dispose();
            }
            if (knownRequest) {
                log.debug("收到取消指令, requestId: {}, reason: {}", finalRequestId, controlMessage.getReason());
            }
        }
    }

    private void acknowledge(Acknowledgment acknowledgment) {
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }

    /**
     * 尝试获取请求处理锁
     *
     * @return true 新请求可处理，false 正在处理或已完成应跳过
     */
    private boolean tryAcquireProcessingLock(String requestId) {
        if (requestId == null) {
            return true;
        }

        long now = System.currentTimeMillis();
        // 清理过期记录
        processedRequests.entrySet().removeIf(e -> now - e.getValue().timestamp() > REQUEST_RETAIN_MS);

        RequestRecord existing = processedRequests.putIfAbsent(requestId, new RequestRecord(RequestState.PROCESSING, now));
        if (existing == null) {
            return true;
        }
        // 已存在：无论 PROCESSING 还是 COMPLETED 都拒绝
        log.debug("请求已存在, requestId: {}, state: {}", requestId, existing.state());
        return false;
    }

    /**
     * 标记完成，保留记录防止重复
     */
    private void markProcessingComplete(String requestId) {
        if (requestId != null) {
            processedRequests.put(requestId, new RequestRecord(RequestState.COMPLETED, System.currentTimeMillis()));
        }
    }

    /**
     * 仅在失败需重试时调用
     */
    private void releaseProcessingLock(String requestId) {
        if (requestId != null) {
            processedRequests.remove(requestId);
        }
    }

    private boolean isCancelled(String requestId) {
        return requestId != null && Boolean.TRUE.equals(cancelledRequests.get(requestId));
    }

    private void clearCancellationFlag(String requestId) {
        if (requestId != null) {
            cancelledRequests.remove(requestId);
        }
    }

    private void processChatRequest(String requestId, KafkaChatRequestDTO request, Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();

        // 获取或创建会话
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();
        UUID userId = sessionVO.getSysUserId();
        request.setSessionId(sessionId);
        if (request.getUserId() == null) {
            request.setUserId(userId);
        }

        if (isCancelled(requestId)) {
            log.debug("请求在处理前已被标记为取消, requestId: {}", requestId);
            markProcessingComplete(requestId);
            acknowledge(acknowledgment);
            clearCancellationFlag(requestId);
            return;
        }

        // 构建消息上下文
        ChatMessageUtil.ChatContext chatContext = ChatMessageUtil.buildContext(sessionId, request, chatMessageRepository);
        List<Message> messages = chatContext.messages();

        // RAG知识检索
        if (Boolean.TRUE.equals(request.getUseRag())) {
            Authentication previousAuth = setTemporaryAuthentication(userId);
            try {
                String ragContext = ChatMessageUtil.retrieveKnowledge(request, knowledgeService);
                if (StringUtils.hasText(ragContext)) {
                    messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
                }
            } finally {
                restoreAuthentication(previousAuth);
            }
        }

        // 保存用户消息（幂等）
        ChatMessageUtil.addUserMessageIfNotDuplicate(sessionId, request.getMessage(),
                request.getAttachments(), request.getFileReferences(), chatContext.lastMessage(),
                requestId, chatMessageRepository);

        // 流式处理
        StringBuffer fullResponse = new StringBuffer();
        String userQuery = request.getMessage();
        UUID courseId = request.getCourseId();

        Disposable subscription = chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(chunk -> handleChunk(requestId, chunk, fullResponse))
                .doOnComplete(() -> onChatComplete(requestId, sessionId, userId, userQuery, courseId,
                        fullResponse.toString(), startTime, acknowledgment))
                .doOnError(error -> onChatError(requestId, sessionId, error,
                        fullResponse.toString(), userId, userQuery, courseId, startTime, acknowledgment))
                .doOnCancel(() -> onChatCancelled(requestId, sessionId, userId, userQuery, courseId,
                        fullResponse.toString(), startTime, acknowledgment))
                .onErrorResume(Exceptions::isCancel, e -> Mono.empty())
                .subscribe();
        activeSubscriptions.put(requestId, subscription);
    }

    private void onChatComplete(String requestId, UUID sessionId, UUID userId, String userQuery,
                                UUID courseId, String response, long startTime, Acknowledgment acknowledgment) {
        clearActiveRequest(requestId);
        ChatMessage assistantMessage = null;
        if (!response.isEmpty()) {
            assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, response, requestId, chatMessageRepository);
            assistantMessage = initializeAudioGenerationSafely(assistantMessage, requestId);
            chatSessionService.updateSessionLastMessage(sessionId, response);
        }

        // 向量化对话
        if (StringUtils.hasText(userQuery) && StringUtils.hasText(response)) {
            UUID messageId = assistantMessage != null ? assistantMessage.getId() : null;
            try {
                knowledgeService.vectorizeChatContent(userQuery, response, sessionId, messageId, courseId, userId);
            } catch (Exception e) {
                log.debug("向量化对话内容失败: sessionId={}, error={}", sessionId, e.getMessage());
            }
        }

        // 如果标记了需要生成标题，异步生成标题
        // 注意：这里需要从request中获取标记，但request不在这个方法的参数中
        // 我们需要从kafkaChatService的requestCache中获取
        try {
            KafkaChatRequestDTO request = kafkaChatService.getRequestFromCache(requestId);
            if (request != null && Boolean.TRUE.equals(request.getNeedGenerateTitle())) {
                // 异步生成标题
                chatSessionService.generateSessionTitleAsync(sessionId);
            }
        } catch (Exception e) {
            log.debug("检查是否需要生成标题失败: requestId={}, sessionId={}, error={}", requestId, sessionId, e.getMessage());
        }

        kafkaChatService.completeResponse(requestId);
        markProcessingComplete(requestId);
        acknowledge(acknowledgment);
        clearCancellationFlag(requestId);
    }

    private void onChatError(String requestId, UUID sessionId, Throwable error, String response,
                             UUID userId, String userQuery, UUID courseId, long startTime,
                             Acknowledgment acknowledgment) {
        boolean interrupted = isConnectionInterrupted(error);
        boolean wasCancelled = isCancelled(requestId);
        if (interrupted && !wasCancelled) {
            onChatComplete(requestId, sessionId, userId, userQuery, courseId, response, startTime, acknowledgment);
            return;
        }
        if (interrupted) {
            // 保存已生成但未完成的回复，仍更新会话，便于追踪
            if (StringUtils.hasText(response)) {
                ChatMessage assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, response, requestId, chatMessageRepository);
                initializeAudioGenerationSafely(assistantMessage, requestId);
                chatSessionService.updateSessionLastMessage(sessionId, response);
                // 取消场景下不进行向量化
            }
            kafkaChatService.completeResponse(requestId);
            markProcessingComplete(requestId);
            acknowledge(acknowledgment);
            clearCancellationFlag(requestId);
            return;
        }
        clearActiveRequest(requestId);
        kafkaChatService.handleError(requestId, error);
        boolean retryable = ChatMessageUtil.isRetryableError(error);
        if (retryable) {
            releaseProcessingLock(requestId);
            clearCancellationFlag(requestId);
        } else {
            markProcessingComplete(requestId);
            acknowledge(acknowledgment);
            clearCancellationFlag(requestId);
        }
    }

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

    private void onChatCancelled(String requestId, UUID sessionId, UUID userId, String userQuery,
                                 UUID courseId, String response, long startTime, Acknowledgment acknowledgment) {
        clearActiveRequest(requestId);
        if (StringUtils.hasText(response)) {
            ChatMessage assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, response, requestId, chatMessageRepository);
            initializeAudioGenerationSafely(assistantMessage, requestId);
            chatSessionService.updateSessionLastMessage(sessionId, response);
        }
        kafkaChatService.completeResponse(requestId);
        markProcessingComplete(requestId);
        acknowledge(acknowledgment);
        clearCancellationFlag(requestId);
    }

    private ChatMessage initializeAudioGenerationSafely(ChatMessage assistantMessage, String requestId) {
        try {
            return ttsAudioService.initializeAudioGeneration(assistantMessage);
        } catch (Exception ex) {
            log.warn("初始化TTS音频生成失败，不影响聊天主流程: requestId={}, messageId={}, error={}",
                    requestId,
                    assistantMessage != null ? assistantMessage.getId() : null,
                    ex.getMessage());
            return assistantMessage;
        }
    }

    private void handleChunk(String requestId, String chunk, StringBuffer accumulator) {
        if (chunk == null) {
            return;
        }
        accumulator.append(chunk);
        kafkaChatService.handleResponse(requestId, chunk);
    }

    private Authentication setTemporaryAuthentication(UUID userId) {
        Authentication previous = SecurityContextHolder.getContext().getAuthentication();
        if (userId == null) {
            return previous;
        }
        Map<String, Object> principal = new HashMap<>();
        principal.put("userId", userId.toString());
        principal.put("username", "kafka-chat-worker");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return previous;
    }

    private void restoreAuthentication(Authentication previous) {
        SecurityContextHolder.getContext().setAuthentication(previous);
    }

    private void clearActiveRequest(String requestId) {
        if (requestId != null) {
            activeSubscriptions.remove(requestId);
        }
    }

    private boolean isConnectionInterrupted(Throwable error) {
        if (error == null) {
            return true;
        }
        if (Exceptions.isCancel(error) || error instanceof java.util.concurrent.CancellationException) {
            return true;
        }
        if (error instanceof java.io.IOException || error instanceof java.net.SocketException
                || error instanceof java.nio.channels.ClosedChannelException) {
            return true;
        }
        String message = error.getMessage();
        if (message != null) {
            String lower = message.toLowerCase();
            if (lower.contains("broken pipe") || lower.contains("connection reset")
                    || lower.contains("connection closed") || lower.contains("channel closed")
                    || lower.contains("connection refused") || lower.contains("client cancelled")) {
                return true;
            }
        }
        Throwable cause = error.getCause();
        if (cause != null && cause != error) {
            return isConnectionInterrupted(cause);
        }
        return false;
    }

    private enum RequestState {PROCESSING, COMPLETED}

    private record RequestRecord(RequestState state, long timestamp) {
    }
}

