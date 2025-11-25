package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
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
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    private final KnowledgeService knowledgeService;
    private final ChatClient chatClient;
    private final KafkaChatService kafkaChatService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseVO chat(ChatRequestDTO request) {
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();

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
                chatContext.lastMessage(), null, chatMessageRepository);

        ChatMessage assistantMessage = ChatMessageUtil.saveAssistantMessage(sessionId, aiResponse, chatMessageRepository);

        chatSessionService.updateSessionLastMessage(sessionId, aiResponse);

        ChatResponseVO responseVO = new ChatResponseVO();
        responseVO.setSessionId(sessionId);
        responseVO.setMessageId(assistantMessage.getId());
        responseVO.setContent(aiResponse);
        responseVO.setModel(AIChatConstants.MODEL_QWEN3_MAX);
        responseVO.setTokenCount(assistantMessage.getTokenCount());
        responseVO.setResponseTime(LocalDateTime.now());
        responseVO.setFinished(true);

        return responseVO;
    }

    @Override
    public Flux<String> chatStream(ChatRequestDTO request) {
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();

        ChatMessageUtil.ChatContext chatContext = buildContext(sessionId, request);
        List<Message> messages = chatContext.messages();

        if (Boolean.TRUE.equals(request.getUseRag())) {
            String ragContext = retrieveKnowledge(request);
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
            }
        }

        ChatMessageUtil.addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(),
                chatContext.lastMessage(), null, chatMessageRepository);

        StringBuilder fullResponse = new StringBuilder();

        return chatClient
                .prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    ChatMessageUtil.saveAssistantMessage(sessionId, fullResponse.toString(), chatMessageRepository);
                    chatSessionService.updateSessionLastMessage(sessionId, fullResponse.toString());
                });
    }

    @Override
    public Flux<String> chatStreamKafka(KafkaChatRequestDTO request) {
        return kafkaChatService.chatStreamKafka(request);
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

        UUID userId = UserContextUtil.getCurrentUserId();
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

}

