package com.dayz.sapientiacloud_edupivot.celestial_hub.common.utils;

import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 聊天消息处理工具类
 * 用于抽取公共的消息处理逻辑
 */
@Slf4j
public class ChatMessageHelper {

    /**
     * 构建消息上下文
     */
    public static List<Message> buildContext(UUID sessionId, ChatRequestDTO request, ChatMessageRepository chatMessageRepository) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));

        // 获取历史消息
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
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

    /**
     * 构建消息上下文（Kafka版本）
     */
    public static List<Message> buildContext(UUID sessionId, KafkaChatRequestDTO request, ChatMessageRepository chatMessageRepository) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));

        // 获取历史消息
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
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

    /**
     * 检索知识（ChatRequestDTO版本）
     */
    public static String retrieveKnowledge(ChatRequestDTO request, KnowledgeService knowledgeService) {
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
            log.error("检索知识失败", e);
        }
        return null;
    }

    /**
     * 检索知识（KafkaChatRequestDTO版本）
     */
    public static String retrieveKnowledge(KafkaChatRequestDTO request, KnowledgeService knowledgeService) {
        try {
            KnowledgeRequestDTO query = new KnowledgeRequestDTO();
            query.setQuery(request.getMessage());
            query.setCourseId(request.getCourseId());
            query.setChapterId(request.getChapterId());
            query.setTopK(AIChatConstants.DEFAULT_RAG_TOP_K);
            query.setSimilarityThreshold(AIChatConstants.DEFAULT_RAG_SIMILARITY_THRESHOLD);

            KnowledgeSearchVO result = knowledgeService.searchKnowledge(query);
            if (result != null && result.getItems() != null && !result.getItems().isEmpty()) {
                return result.getItems().stream()
                        .map(item -> String.format(AIChatConstants.RAG_ITEM_FORMAT,
                                item.getTitle(),
                                item.getContentType(),
                                item.getContent()))
                        .collect(Collectors.joining(AIChatConstants.RAG_SEPARATOR));
            }
        } catch (Exception e) {
            log.error("检索知识失败", e);
        }
        return null;
    }

    /**
     * 估算token数量
     */
    public static Integer estimateTokens(String text) {
        return (int) (text.length() * AIChatConstants.TOKEN_ESTIMATE_RATIO);
    }

    /**
     * 判断错误是否可重试
     */
    public static boolean isRetryableError(Throwable error) {
        if (error == null) {
            return false;
        }
        
        String errorMessage = error.getMessage();
        if (errorMessage == null) {
            return false;
        }
        
        // 网络相关错误可重试
        if (errorMessage.contains("timeout") || 
            errorMessage.contains("connection") ||
            errorMessage.contains("network") ||
            errorMessage.contains("retry")) {
            return true;
        }
        
        // 服务不可用可重试
        if (error instanceof org.springframework.web.client.ResourceAccessException ||
            error instanceof java.net.SocketTimeoutException ||
            error instanceof java.net.ConnectException) {
            return true;
        }
        
        return false;
    }
}

