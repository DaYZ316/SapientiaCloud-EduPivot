package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatResponseVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatMessageService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;
    private final KnowledgeService knowledgeService;
    private final ChatClient chatClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatResponseVO chat(ChatRequestDTO request) {
        ChatSessionVO sessionVO = getOrCreateSession(request);
        UUID sessionId = sessionVO.getId();

        List<Message> messages = buildContext(sessionId, request);

        if (Boolean.TRUE.equals(request.getUseRag())) {
            String ragContext = retrieveKnowledge(request);
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
            }
        }

        String aiResponse = callModel(messages);

        addUserMessage(sessionId, request.getMessage(), request.getAttachments());

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

        List<Message> messages = buildContext(sessionId, request);

        if (Boolean.TRUE.equals(request.getUseRag())) {
            String ragContext = retrieveKnowledge(request);
            if (StringUtils.hasText(ragContext)) {
                messages.add(new SystemMessage(AIChatConstants.RAG_PREFIX + ragContext));
            }
        }

        addUserMessage(sessionId, request.getMessage(), request.getAttachments());

        StringBuilder fullResponse = new StringBuilder();

        return chatClient
                .prompt()
                .messages(messages)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    try {
                        saveAssistantMessage(sessionId, fullResponse.toString());
                        chatSessionService.updateSessionLastMessage(sessionId, fullResponse.toString());
                    } catch (Exception e) {
                        log.error("保存流式对话结果失败: ", e);
                    }
                })
                .doOnError(e -> log.error("流式对话异常，会话ID: {}, 错误信息: ", sessionId, e));
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

    private List<Message> buildContext(UUID sessionId, ChatRequestDTO request) {
        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));

        List<ChatMessage> history = listMessagesBySessionId(sessionId, AIChatConstants.DEFAULT_HISTORY_LIMIT);
        for (ChatMessage msg : history) {
            if (AIChatConstants.ROLE_USER.equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_ASSISTANT.equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_SYSTEM.equals(msg.getRole())) {
                messages.add(new SystemMessage(msg.getContent()));
            }
        }

        messages.add(new UserMessage(request.getMessage()));

        return messages;
    }

    private String retrieveKnowledge(ChatRequestDTO request) {
        try {
            KnowledgeRequestDTO query = new KnowledgeRequestDTO();
            query.setQuery(request.getMessage());
            query.setCourseId(request.getCourseId());
            query.setChapterId(request.getChapterId());
            query.setTopK(AIChatConstants.DEFAULT_RAG_TOP_K);
            query.setSimilarityThreshold(AIChatConstants.DEFAULT_RAG_SIMILARITY_THRESHOLD);

            KnowledgeSearchVO result = knowledgeService.searchKnowledge(query);
            if (result != null && !CollectionUtils.isEmpty(result.getItems())) {
                return result.getItems().stream()
                        .map(item -> String.format(AIChatConstants.RAG_ITEM_FORMAT,
                                item.getTitle(),
                                item.getContentType(),
                                item.getContent()))
                        .collect(Collectors.joining(AIChatConstants.RAG_SEPARATOR));
            }
        } catch (Exception e) {
            log.error("知识检索失败: ", e);
        }
        return null;
    }

    private String callModel(List<Message> messages) {
        try {
            return chatClient
                    .prompt()
                    .messages(messages)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI模型调用失败: ", e);
            throw new BusinessException(AIChatEnum.AI_SERVICE_ERROR);
        }
    }

    private ChatMessage addUserMessage(UUID sessionId, String content, List<String> attachments) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_USER);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setAttachments(attachments);
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        return chatMessageRepository.save(message);
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
        return (int) (text.length() * AIChatConstants.TOKEN_ESTIMATE_RATIO);
    }
}

