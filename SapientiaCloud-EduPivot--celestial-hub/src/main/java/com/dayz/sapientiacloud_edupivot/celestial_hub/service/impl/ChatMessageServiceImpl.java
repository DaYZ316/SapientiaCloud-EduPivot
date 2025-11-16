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
import com.github.f4b6a3.uuid.UuidCreator;
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
import java.util.Objects;
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

        addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(), chatContext.lastMessage(), null);

        ChatMessage assistantMessage = saveAssistantMessage(sessionId, aiResponse);

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

        addUserMessageIfNotDuplicate(sessionId, request.getMessage(), request.getAttachments(), chatContext.lastMessage(), null);

        StringBuilder fullResponse = new StringBuilder();

        return chatClient
                .prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    saveAssistantMessage(sessionId, fullResponse.toString());
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

    private ChatMessage saveAssistantMessage(UUID sessionId, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_ASSISTANT);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        message.setTokenCount(estimateTokens(content));
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        return chatMessageRepository.save(message);
    }

    private Integer estimateTokens(String text) {
        // 使用工具类估算token
        return ChatMessageUtil.estimateTokens(text);
    }

    /**
     * 仅当与最后一条用户消息不相同时才保存；若提供 requestId 则走幂等插入
     */
    private ChatMessage addUserMessageIfNotDuplicate(UUID sessionId, String content, List<String> attachments,
                                                     ChatMessage lastMessageFromContext, String requestId) {
        if (requestId != null) {
            // 幂等插入，数据库层唯一索引保障
            return addUserMessage(sessionId, content, attachments, requestId);
        }
        ChatMessage last = lastMessageFromContext;
        if (last != null
                && Objects.equals(last.getRole(), AIChatConstants.ROLE_USER)
                && Objects.equals(last.getContent(), content)
                && equalAttachments(last.getAttachments(), attachments)) {
            return last;
        }
        return addUserMessage(sessionId, content, attachments, null);
    }

    private boolean equalAttachments(List<String> a, List<String> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        // 顺序一致时直接比较；若未来需要无序比较，可改为比较为Set
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }
}

