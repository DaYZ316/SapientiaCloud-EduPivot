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
import com.github.f4b6a3.uuid.UuidCreator;
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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Kafka聊天消息消费者
 * 从Kafka接收聊天请求，调用Spring AI处理，并将结果发送回Kafka
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaChatConsumer {

    private final ChatClient chatClient;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    private final KnowledgeService knowledgeService;
    private final KafkaChatService kafkaChatService;

    /**
     * 监听聊天请求主题
     */
    @KafkaListener(topics = "${spring.kafka.topic.chat-request:chat-request-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}")
    public void consumeChatRequest(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_KEY) String requestId,
                                   Acknowledgment acknowledgment) {
        long startTime = System.currentTimeMillis();
        try {
            log.debug("收到Kafka聊天请求, requestId: {}", requestId);

            // 解析请求消息
            KafkaChatService.ChatRequestMessage requestMessage =
                    JSON.parseObject(message, KafkaChatService.ChatRequestMessage.class);

            // 如果消息中没有requestId，使用header中的requestId
            if (requestMessage.getRequestId() == null || requestMessage.getRequestId().isEmpty()) {
                requestMessage.setRequestId(requestId);
            } else {
                // 使用消息中的requestId
                requestId = requestMessage.getRequestId();
            }

            KafkaChatRequestDTO request = requestMessage.getRequest();
            if (request == null) {
                throw new IllegalArgumentException("请求消息不能为空");
            }

            // 处理聊天请求
            processChatRequest(requestId, request);

            // 手动提交偏移量（处理成功后才提交）
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

            log.debug("Kafka聊天请求处理成功, requestId: {}, 耗时: {}ms", requestId,
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("处理Kafka聊天请求失败, requestId: {}, 耗时: {}ms", requestId,
                    System.currentTimeMillis() - startTime, e);

            // 通知客户端错误
            if (requestId != null) {
                kafkaChatService.handleError(requestId, e);
            }

            // 根据错误类型决定是否确认消息
            // 可重试的错误不确认，让Kafka重试；不可重试的错误确认，避免无限重试
            if (acknowledgment != null) {
                if (ChatMessageUtil.isRetryableError(e)) {
                    log.warn("错误可重试，不确认消息，等待重试, requestId: {}", requestId);
                    // 不确认，等待重试
                } else {
                    log.warn("错误不可重试，确认消息, requestId: {}", requestId);
                    acknowledgment.acknowledge();
                }
            }
        }
    }

    /**
     * 处理聊天请求
     */
    private void processChatRequest(String requestId, KafkaChatRequestDTO request) {
        final long startTime = System.currentTimeMillis();
        final String finalRequestId = requestId;

        try {
            // 获取或创建会话
            ChatSessionVO sessionVO = getOrCreateSession(request);
            final UUID sessionId = sessionVO.getId();
            final UUID userId = sessionVO.getSysUserId();

            // 构建消息上下文（使用工具类）
            ChatMessageUtil.ChatContext chatContext = ChatMessageUtil.buildContext(sessionId, request, chatMessageRepository);
            List<Message> messages = chatContext.messages();

            // 如果使用RAG，添加知识检索结果（使用工具类）
            if (Boolean.TRUE.equals(request.getUseRag())) {
                String ragContext = ChatMessageUtil.retrieveKnowledge(request, knowledgeService);
                if (StringUtils.hasText(ragContext)) {
                    messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
                }
            }

            // 先保存用户消息（在AI调用前保存），带去重/幂等以避免重试重复
            ChatMessage userMessage = addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(), chatContext.lastMessage(), finalRequestId);

            // 使用流式处理，将结果发送到Kafka响应主题
            final StringBuilder fullResponse = new StringBuilder();
            final String userQuery = request.getMessage();

            chatClient
                    .prompt()
                    .messages(messages)
                    .stream()
                    .content()
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnNext(chunk -> {
                        // 将每个chunk发送到响应主题
                        fullResponse.append(chunk);
                        kafkaChatService.handleResponse(finalRequestId, chunk);
                    })
                    .doOnComplete(() -> {
                        // AI调用成功后才保存助手消息
                        String completeResponse = fullResponse.toString();
                        ChatMessage assistantMessage = null;
                        if (!completeResponse.isEmpty()) {
                            assistantMessage = saveAssistantMessage(sessionId, completeResponse);
                            chatSessionService.updateSessionLastMessage(sessionId, completeResponse);
                        }

                        // 向量化对话内容（Q&A对格式）
                        if (userQuery != null && !userQuery.trim().isEmpty()
                                && completeResponse != null && !completeResponse.trim().isEmpty()) {
                            UUID messageId = assistantMessage != null ? assistantMessage.getId() : null;
                            UUID courseId = request.getCourseId();
                            try {
                                knowledgeService.vectorizeChatContent(userQuery, completeResponse, sessionId, messageId, courseId, userId);
                            } catch (Exception e) {
                                log.warn("向量化对话内容失败，但不影响聊天流程: sessionId={}, messageId={}, userId={}, error={}",
                                        sessionId, messageId, userId, e.getMessage());
                            }
                        }

                        // 完成响应流
                        kafkaChatService.completeResponse(finalRequestId);
                        log.debug("聊天请求处理完成, requestId: {}, sessionId: {}, 耗时: {}ms",
                                finalRequestId, sessionId, System.currentTimeMillis() - startTime);
                    })
                    .doOnError(error -> {
                        log.error("处理聊天请求时发生错误, requestId: {}, sessionId: {}, 耗时: {}ms",
                                finalRequestId, sessionId, System.currentTimeMillis() - startTime, error);
                        kafkaChatService.handleError(finalRequestId, error);
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("处理聊天请求失败, requestId: {}, 耗时: {}ms",
                    finalRequestId, System.currentTimeMillis() - startTime, e);
            kafkaChatService.handleError(finalRequestId, e);
            // TODO 如果处理失败，可以考虑回滚已保存的用户消息
            // 但为了数据完整性，这里保留用户消息
            throw e;
        }
    }

    /**
     * 获取或创建会话
     */
    private ChatSessionVO getOrCreateSession(KafkaChatRequestDTO request) {
        if (request.getSessionId() != null) {
            return chatSessionService.getChatSessionById(request.getSessionId());
        }
        // 从请求中获取userId，如果没有则抛出异常
        UUID userId = request.getUserId();
        if (userId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }
        return chatSessionService.addChatSession(userId, request.getCourseId(), request.getSessionType(), null);
    }

    /**
     * 添加用户消息
     */
    private ChatMessage addUserMessage(UUID sessionId, String content, List<String> attachments, String requestId) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_USER);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setAttachments(attachments);
        message.setRequestId(requestId);
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        try {
            return chatMessageRepository.save(message);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            if (requestId != null) {
                return chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(sessionId, AIChatConstants.ROLE_USER, requestId);
            }
            throw dup;
        }
    }

    /**
     * 保存助手消息
     */
    private ChatMessage saveAssistantMessage(UUID sessionId, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_ASSISTANT);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        message.setTokenCount(ChatMessageUtil.estimateTokens(content));
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    /**
     * 仅当与最后一条用户消息不相同时才保存，避免重试导致重复
     */
    private ChatMessage addUserMessageIfNotDuplicate(UUID sessionId, String content, List<String> attachments,
                                                     ChatMessage lastMessageFromContext, String requestId) {
        if (requestId != null) {
            return addUserMessage(sessionId, content, attachments, requestId);
        }
        ChatMessage last = lastMessageFromContext;
        if (last != null
                && Objects.equals(last.getRole(), AIChatConstants.ROLE_USER)
                && Objects.equals(last.getContent(), content)
                && ChatMessageUtil.attachmentsEqual(last.getAttachments(), attachments)) {
            return last;
        }
        return addUserMessage(sessionId, content, attachments, null);
    }

}
