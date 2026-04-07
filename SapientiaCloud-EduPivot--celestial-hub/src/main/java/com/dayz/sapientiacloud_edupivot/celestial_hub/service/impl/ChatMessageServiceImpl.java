package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatResponseVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatMessageService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KafkaChatService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.TtsAudioService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    private final KnowledgeService knowledgeService;
    private final ChatClient chatClient;
    private final KafkaChatService kafkaChatService;
    private final TtsAudioService ttsAudioService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseVO chat(ChatRequestDTO request) {
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);

        ChatMessageUtil.ChatContext chatContext = buildContext(sessionId, request);
        List<Message> messages = chatContext.messages();

        if (Boolean.TRUE.equals(request.getUseRag())) {
            String ragContext = retrieveKnowledge(request);
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
            }
        }

        String aiResponse = callModel(messages);

        ChatMessageUtil.addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(),
                request.getFileReferences(), chatContext.lastMessage(), null, chatMessageRepository);

        ChatMessage assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, aiResponse, chatMessageRepository);
        assistantMessage = ttsAudioService.initializeAudioGeneration(assistantMessage);

        chatSessionService.updateSessionLastMessage(sessionId, aiResponse);

        // 向量化对话内容（Q&A对格式）
        UUID userId = sessionVO.getSysUserId();
        if (userId != null && request.getMessage() != null && !request.getMessage().trim().isEmpty()
                && aiResponse != null && !aiResponse.trim().isEmpty()) {
            try {
                knowledgeService.vectorizeChatContent(
                        request.getMessage(),
                        aiResponse,
                        sessionId,
                        assistantMessage.getId(),
                        request.getCourseId(),
                        userId
                );
            } catch (Exception e) {
                log.warn("向量化对话内容失败，但不影响聊天流程: sessionId={}, messageId={}, userId={}, error={}",
                        sessionId, assistantMessage.getId(), userId, e.getMessage());
            }
        }

        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setSessionId(sessionId);
        responseVO.setMessageId(assistantMessage.getId());
        responseVO.setContent(aiResponse);
        responseVO.setModel(AIChatConstants.MODEL_QWEN3_MAX);
        responseVO.setTokenCount(assistantMessage.getTokenCount());
        responseVO.setFileReferences(request.getFileReferences());
        responseVO.setResponseTime(LocalDateTime.now());
        responseVO.setFinished(true);
        responseVO.setAudioStatus(assistantMessage.getAudioStatus());
        responseVO.setAudioUrl(assistantMessage.getAudioUrl());
        responseVO.setAudioFormat(assistantMessage.getAudioFormat());
        responseVO.setAudioTaskId(assistantMessage.getAudioTaskId());

        return responseVO;
    }

    @Override
    public Flux<String> chatStream(ChatRequestDTO request) {
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);

        ChatMessageUtil.ChatContext chatContext = buildContext(sessionId, request);
        List<Message> messages = chatContext.messages();

        if (Boolean.TRUE.equals(request.getUseRag())) {
            String ragContext = retrieveKnowledge(request);
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
            }
        }

        ChatMessageUtil.addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(),
                request.getFileReferences(), chatContext.lastMessage(), null, chatMessageRepository);

        StringBuilder fullResponse = new StringBuilder();
        final UUID userId = sessionVO.getSysUserId();
        final String userQuery = request.getMessage();
        final UUID courseId = request.getCourseId();
        AtomicBoolean responsePersisted = new AtomicBoolean(false);

        return chatClient
                .prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnCancel(() -> persistStreamResponse(sessionId, fullResponse.toString(), userId, userQuery, courseId, false, responsePersisted))
                .doOnError(error -> persistStreamResponse(sessionId, fullResponse.toString(), userId, userQuery, courseId, false, responsePersisted))
                .doOnComplete(() -> persistStreamResponse(sessionId, fullResponse.toString(), userId, userQuery, courseId, true, responsePersisted));
    }

    @Override
    public Flux<String> chatStreamKafka(KafkaChatRequestDTO request) {
        return kafkaChatService.chatStreamKafka(request);
    }

    @Override
    public void cancelKafkaChat(String requestId, String reason) {
        if (!StringUtils.hasText(requestId)) {
            throw new BusinessException(AIChatEnum.REQUEST_ID_REQUIRED);
        }
        // 通过接口调用时，立即中断Kafka任务并清理资源
        kafkaChatService.cancelAndCleanup(requestId, StringUtils.hasText(reason) ? reason : "manual_cancel");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> listMessagesBySessionId(UUID sessionId, Integer limit) {
        if (sessionId == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        if (limit != null && limit > 0) {
            Pageable pageable = PageRequest.of(0, limit);
            return chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId, pageable).getContent();
        }

        return chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessage getChatMessageById(UUID id) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.MESSAGE_ID_REQUIRED);
        }

        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.MESSAGE_NOT_EXISTS));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean feedbackMessage(UUID messageId, Integer feedback) {
        if (messageId == null) {
            throw new BusinessException(AIChatEnum.MESSAGE_ID_REQUIRED);
        }

        if (feedback == null || (feedback != AIChatConstants.FEEDBACK_LIKE && feedback != AIChatConstants.FEEDBACK_DISLIKE)) {
            throw new BusinessException(AIChatEnum.FEEDBACK_VALUE_INVALID);
        }

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(AIChatEnum.MESSAGE_NOT_EXISTS));

        message.setIsFeedback(feedback);
        message.setUpdateTime(LocalDateTime.now());
        chatMessageRepository.save(message);

        return true;
    }

    private ChatSessionVO getOrCreateSession(ChatRequestDTO request) {
        if (request.getSessionId() != null) {
            return chatSessionService.getChatSessionById(request.getSessionId());
        }

        return chatSessionService.addChatSession(request.getCourseId(), request.getSessionType(), null);
    }

    private ChatMessageUtil.ChatContext buildContext(UUID sessionId, ChatRequestDTO request) {
        return ChatMessageUtil.buildContext(sessionId, request, chatMessageRepository);
    }

    private String retrieveKnowledge(ChatRequestDTO request) {
        // 使用工具类检索知识
        return ChatMessageUtil.retrieveKnowledge(request, knowledgeService);
    }

    private String callModel(List<Message> messages) {
        try {
            return chatClient
                    .prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new BusinessException(AIChatEnum.AI_SERVICE_ERROR);
        }
    }

    private void persistStreamResponse(UUID sessionId, String response, UUID userId, String userQuery,
                                       UUID courseId, boolean vectorize, AtomicBoolean persistedFlag) {
        if (!StringUtils.hasText(response)) {
            return;
        }
        if (!persistedFlag.compareAndSet(false, true)) {
            return;
        }
        ChatMessage assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, response, chatMessageRepository);
        assistantMessage = ttsAudioService.initializeAudioGeneration(assistantMessage);
        chatSessionService.updateSessionLastMessage(sessionId, response);

        if (!vectorize) {
            return;
        }
        if (userId == null || !StringUtils.hasText(userQuery)) {
            return;
        }
        try {
            knowledgeService.vectorizeChatContent(
                    userQuery,
                    response,
                    sessionId,
                    assistantMessage.getId(),
                    courseId,
                    userId
            );
        } catch (Exception e) {
            log.warn("向量化对话内容失败，但不影响聊天流程: sessionId={}, messageId={}, userId={}, error={}",
                    sessionId, assistantMessage.getId(), userId, e.getMessage());
        }
    }

}

