package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchResultVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ChatRoleEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 聊天消息处理工具类
 * 用于抽取公共的消息处理逻辑
 */
@Slf4j
public class ChatMessageUtil {

    /**
     * 构建消息上下文（返回上下文消息与最后一条历史消息）
     */
    public static ChatContext buildContext(UUID sessionId, ChatRequestDTO request, ChatMessageRepository chatMessageRepository) {
        return buildResendAwareContext(
                sessionId,
                request.getMessage(),
                request.getAttachments(),
                request.getFileReferences(),
                request.getResendSourceUserMessageId(),
                request.getResendSourceRequestId(),
                chatMessageRepository
        );
    }

    private static ChatContext buildContext(UUID sessionId,
                                            String message,
                                            List<String> attachments,
                                            List<FileReference> fileReferences,
                                            ChatMessageRepository chatMessageRepository) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));
        messages.add(new SystemMessage(AIChatConstants.MATH_LATEX_STYLE_PROMPT));

        // 获取历史消息
        // 只有当最后一条历史消息不是同一条用户消息时才追加当前请求
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        ChatMessage last = history.isEmpty() ? null : history.get(history.size() - 1);
        appendHistoryMessages(messages, history);
        if (!isSameAsLastUserMessage(last, message, attachments, fileReferences)) {
            messages.add(new UserMessage(message));
        }
        return new ChatContext(messages, last);
    }

    private static ChatContext buildResendAwareContext(UUID sessionId,
                                                       String message,
                                                       List<String> attachments,
                                                       List<FileReference> fileReferences,
                                                       UUID resendSourceUserMessageId,
                                                       String resendSourceRequestId,
                                                       ChatMessageRepository chatMessageRepository) {
        boolean resendRequest = resendSourceUserMessageId != null || StringUtils.hasText(resendSourceRequestId);
        if (!resendRequest) {
            return buildContext(sessionId, message, attachments, fileReferences, chatMessageRepository);
        }

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));
        messages.add(new SystemMessage(AIChatConstants.MATH_LATEX_STYLE_PROMPT));

        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        ChatMessage resendSourceUserMessage = resolveResendSourceUserMessage(history, resendSourceUserMessageId, resendSourceRequestId);
        List<ChatMessage> contextHistory = trimHistoryBeforeSourceMessage(history, resendSourceUserMessage);

        ChatMessage last = contextHistory.isEmpty() ? null : contextHistory.get(contextHistory.size() - 1);
        appendHistoryMessages(messages, contextHistory);
        messages.add(new UserMessage(message));
        return new ChatContext(messages, last);
    }

    private static void appendHistoryMessages(List<Message> messages, List<ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        for (ChatMessage msg : history) {
            Message historyMessage = toContextMessage(msg);
            if (historyMessage != null) {
                messages.add(historyMessage);
            }
        }
    }

    private static Message toContextMessage(ChatMessage msg) {
        if (msg == null || msg.getRole() == null) {
            return null;
        }

        String content = buildContextContent(msg);
        if (!StringUtils.hasText(content)) {
            return null;
        }

        Integer role = msg.getRole();
        if (AIChatConstants.ROLE_USER.equals(role)
                || Integer.valueOf(ChatRoleEnum.QUESTION_REQUESTER.getCode()).equals(role)) {
            return new UserMessage(content);
        }
        if (AIChatConstants.ROLE_ASSISTANT.equals(role)
                || Integer.valueOf(ChatRoleEnum.QUESTION_GENERATOR.getCode()).equals(role)) {
            return new AssistantMessage(content);
        }
        if (AIChatConstants.ROLE_SYSTEM.equals(role)) {
            return new SystemMessage(content);
        }
        return null;
    }

    private static String buildContextContent(ChatMessage msg) {
        if (msg == null || msg.getRole() == null) {
            return null;
        }

        Integer role = msg.getRole();
        if (Integer.valueOf(ChatRoleEnum.QUESTION_REQUESTER.getCode()).equals(role)) {
            return buildQuestionRequestHistory(msg);
        }
        if (Integer.valueOf(ChatRoleEnum.QUESTION_GENERATOR.getCode()).equals(role)) {
            return buildQuestionResponseHistory(msg);
        }
        return StringUtils.hasText(msg.getContent()) ? msg.getContent() : null;
    }

    private static String buildQuestionRequestHistory(ChatMessage msg) {
        return mergeStructuredQuestionHistory(
                "Previous question-generation request in this session:",
                msg.getContent(),
                "Structured request payload:",
                msg.getQuestionRequest()
        );
    }

    private static String buildQuestionResponseHistory(ChatMessage msg) {
        return mergeStructuredQuestionHistory(
                "Previous question-generation result in this session:",
                msg.getContent(),
                "Structured generated questions:",
                msg.getQuestionResponse()
        );
    }

    private static String mergeStructuredQuestionHistory(String header,
                                                         String summary,
                                                         String payloadLabel,
                                                         String payload) {
        boolean hasSummary = StringUtils.hasText(summary);
        boolean hasPayload = StringUtils.hasText(payload);
        if (!hasSummary && !hasPayload) {
            return null;
        }

        StringBuilder builder = new StringBuilder(header);
        if (hasSummary) {
            builder.append('\n').append(summary.trim());
        }
        if (hasPayload) {
            builder.append('\n').append(payloadLabel).append('\n').append(payload.trim());
        }
        return builder.toString();
    }

    /**
     * 构建消息上下文（Kafka版本，返回上下文消息与最后一条历史消息）
     */
    public static ChatContext buildContext(UUID sessionId, KafkaChatRequestDTO request, ChatMessageRepository chatMessageRepository) {
        return buildResendAwareContext(
                sessionId,
                request.getMessage(),
                request.getAttachments(),
                request.getFileReferences(),
                request.getResendSourceUserMessageId(),
                request.getResendSourceRequestId(),
                chatMessageRepository
        );
    }
    /**
     * 检索知识（ChatRequestDTO版本：知识库 + 文件内容）
     */
    public static String retrieveKnowledge(ChatRequestDTO request, KnowledgeService knowledgeService) {
        log.debug("开始检索知识（ChatRequestDTO版本），会话ID: {}, 用户ID: {}, 消息长度: {}, 文件引用数量: {}",
                request.getSessionId(),
                request.getUserId(),
                request.getMessage() != null ? request.getMessage().length() : 0,
                request.getFileReferences() != null ? request.getFileReferences().size() : 0);

        StringBuilder context = new StringBuilder();
        appendKnowledgeSearchContext(context, request.getSessionId(), request.getMessage(), knowledgeService);
        appendFileVectorContext(context, request.getFileReferences(), request.getUserId(), request.getSessionId(), knowledgeService);
        appendFileContext(context, extractFileIds(request.getFileReferences()), request.getMessage(), knowledgeService);

        String result = !context.isEmpty() ? context.toString() : null;
        log.debug("检索知识完成（ChatRequestDTO版本），上下文长度: {}", result != null ? result.length() : 0);
        return result;
    }

    /**
     * 检索知识（KafkaChatRequestDTO版本：知识库 + 文件内容）
     */
    public static String retrieveKnowledge(KafkaChatRequestDTO request, KnowledgeService knowledgeService) {
        log.debug("开始检索知识（KafkaChatRequestDTO版本），会话ID: {}, 用户ID: {}, 消息长度: {}, 文件引用数量: {}",
                request.getSessionId(),
                request.getUserId(),
                request.getMessage() != null ? request.getMessage().length() : 0,
                request.getFileReferences() != null ? request.getFileReferences().size() : 0);

        StringBuilder context = new StringBuilder();
        appendKnowledgeSearchContext(context, request.getSessionId(), request.getMessage(), knowledgeService);
        appendFileVectorContext(context, request.getFileReferences(), request.getUserId(), request.getSessionId(), knowledgeService);
        appendFileContext(context, extractFileIds(request.getFileReferences()), request.getMessage(), knowledgeService);

        String result = !context.isEmpty() ? context.toString() : null;
        log.debug("检索知识完成（KafkaChatRequestDTO版本），上下文长度: {}", result != null ? result.length() : 0);
        return result;
    }

    private static void appendKnowledgeSearchContext(StringBuilder context, UUID sessionId, String message,
                                                     KnowledgeService knowledgeService) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        try {
            KnowledgeSearchRequestDTO searchRequest = new KnowledgeSearchRequestDTO();
            searchRequest.setQuery(message);
            searchRequest.setTopK(AIChatConstants.DEFAULT_RAG_TOP_K);
            searchRequest.setSimilarityThreshold(AIChatConstants.DEFAULT_RAG_SIMILARITY_THRESHOLD);
            searchRequest.setSessionId(sessionId);

            List<KnowledgeSearchResultVO> results = knowledgeService.searchKnowledge(searchRequest);
            if (results == null || results.isEmpty()) {
                return;
            }

            if (!context.isEmpty()) {
                context.append(AIChatConstants.RAG_SEPARATOR);
            }
            context.append("【知识库】\n");

            int index = 1;
            for (KnowledgeSearchResultVO result : results) {
                if (index > 1) {
                    context.append('\n');
                }
                String label = "片段" + index;
                String title = StringUtils.hasText(result.getTitle()) ? result.getTitle() : label;
                String content = result.getContent() != null ? result.getContent() : "";
                context.append(String.format(AIChatConstants.RAG_ITEM_FORMAT, label, title, content));
                index++;
            }
        } catch (Exception e) {
            log.error("检索知识库内容失败", e);
        }
    }

    /**
     * 添加文件向量内容索引（根据fileReferences中的id字段通过contentId查询KnowledgeVector）
     */
    private static void appendFileVectorContext(StringBuilder context, List<FileReference> fileReferences,
                                                UUID userId, UUID sessionId, KnowledgeService knowledgeService) {
        try {
            if (fileReferences == null || fileReferences.isEmpty()) {
                log.debug("添加文件向量内容索引：文件引用列表为空，跳过处理");
                return;
            }

            log.debug("开始添加文件向量内容索引，文件引用数量: {}, 用户ID: {}, 会话ID: {}",
                    fileReferences.size(), userId, sessionId);

            // 根据文件引用查询向量数据（带验证：检查用户ID和会话ID）
            List<KnowledgeVector> vectors = knowledgeService.findVectorsByFileReferences(fileReferences, userId, sessionId);
            if (vectors == null || vectors.isEmpty()) {
                log.debug("添加文件向量内容索引：未找到对应的向量数据或未通过验证");
                return;
            }

            log.debug("添加文件向量内容索引：找到 {} 条向量数据，开始构建上下文", vectors.size());

            // 构建文件向量内容上下文
            if (context.length() > 0) {
                context.append(AIChatConstants.RAG_SEPARATOR);
            }
            context.append("【文件向量内容】\n");

            int index = 1;
            int addedCount = 0;
            for (KnowledgeVector vector : vectors) {
                if (vector == null) {
                    log.debug("添加文件向量内容索引：跳过空向量，索引位置: {}", index);
                    continue;
                }

                if (index > 1) {
                    context.append('\n');
                }

                String label = "片段" + index;
                String title = StringUtils.hasText(vector.getTitle()) ? vector.getTitle() : label;
                String content = vector.getContent() != null ? vector.getContent() : "";

                if (StringUtils.hasText(content)) {
                    context.append(String.format(AIChatConstants.RAG_ITEM_FORMAT, label, title, content));
                    addedCount++;
                    log.debug("添加文件向量内容索引：已添加向量片段 {}，标题: {}, 内容长度: {}",
                            index, title, content.length());
                    index++;
                } else {
                    log.debug("添加文件向量内容索引：跳过空内容向量，索引位置: {}, 标题: {}", index, title);
                }
            }

            log.debug("添加文件向量内容索引：完成，共添加 {} 个向量片段到上下文", addedCount);
        } catch (Exception e) {
            log.error("检索文件向量内容失败，错误信息: {}", e.getMessage(), e);
        }
    }

    private static void appendFileContext(StringBuilder context, List<UUID> fileIds, String message,
                                          KnowledgeService knowledgeService) {
        try {
            if (fileIds != null && !fileIds.isEmpty()) {
                String fileContext = knowledgeService.retrieveFileContext(
                        fileIds,
                        message,
                        AIChatConstants.DEFAULT_RAG_TOP_K
                );
                if (fileContext != null && !fileContext.isEmpty()) {
                    if (context.length() > 0) {
                        context.append(AIChatConstants.RAG_SEPARATOR);
                    }
                    context.append("【文件内容】\n").append(fileContext);
                }
            }
        } catch (Exception e) {
            log.error("检索文件内容失败", e);
        }
    }

    private static List<UUID> extractFileIds(List<FileReference> references) {
        if (references == null || references.isEmpty()) {
            return null;
        }
        List<UUID> ids = new ArrayList<>();
        for (FileReference reference : references) {
            if (reference != null && reference.getId() != null) {
                ids.add(reference.getId());
            }
        }
        return ids.isEmpty() ? null : ids;
    }

    /**
     * 估算token数量
     */
    public static Integer estimateTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
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
        return error instanceof org.springframework.web.client.ResourceAccessException ||
                error instanceof java.net.SocketTimeoutException ||
                error instanceof java.net.ConnectException;
    }

    /**
     * 判断当前请求是否与最后一条用户消息相同（用于避免上下文重复追加）
     */
    private static boolean isSameAsLastUserMessage(ChatMessage last, String content, List<String> attachments,
                                                   List<FileReference> fileReferences) {
        if (last == null) {
            return false;
        }
        if (!AIChatConstants.ROLE_USER.equals(last.getRole())) {
            return false;
        }
        if (!Objects.equals(last.getContent(), content)) {
            return false;
        }
        if (!equalAttachments(last.getAttachments(), attachments)) {
            return false;
        }
        return equalFileReferences(last.getFileReferences(), fileReferences);
    }

    private static boolean equalAttachments(List<String> a, List<String> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalFileReferences(List<FileReference> a, List<FileReference> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            FileReference left = a.get(i);
            FileReference right = b.get(i);
            UUID leftId = left != null ? left.getId() : null;
            UUID rightId = right != null ? right.getId() : null;
            if (!Objects.equals(leftId, rightId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 对外提供的附件列表比较方法，供其他组件复用，避免重复实现
     */
    public static boolean attachmentsEqual(List<String> a, List<String> b) {
        return equalAttachments(a, b);
    }

    /**
     * 创建用户消息
     *
     * @param sessionId             会话ID
     * @param content               消息内容
     * @param attachments           附件列表
     * @param fileReferences        引用文件列表
     * @param requestId             请求ID（用于幂等性）
     * @param chatMessageRepository 消息仓库
     * @return 保存的用户消息
     */
    public static ChatMessage addUserMessage(UUID sessionId, String content, List<String> attachments,
                                             List<FileReference> fileReferences, String requestId,
                                             ChatMessageRepository chatMessageRepository) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_USER);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setAttachments(attachments);
        message.setFileReferences(fileReferences);
        message.setRequestId(requestId);
        message.setMetadata(buildUserVariantMetadata(message.getId(), requestId));
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setAudioStatus(AIChatConstants.AUDIO_STATUS_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        try {
            return chatMessageRepository.save(message);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            if (requestId != null) {
                return chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                        sessionId, AIChatConstants.ROLE_USER, requestId);
            }
            throw dup;
        }
    }

    /**
     * 保存助手消息
     *
     * @param sessionId             会话ID
     * @param content               消息内容
     * @param chatMessageRepository 消息仓库
     * @return 保存的助手消息
     */
    public static ChatMessage saveAssistantMessage(UUID sessionId, String content,
                                                   ChatMessageRepository chatMessageRepository) {
        return saveAssistantMessage(sessionId, content, null, null, chatMessageRepository);
    }

    /**
     * 保存助手消息（可带 requestId，用于幂等）
     *
     * @param sessionId             会话ID
     * @param content               消息内容
     * @param requestId             请求ID（用于幂等）
     * @param chatMessageRepository 消息仓库
     * @return 保存的助手消息
     */
    public static ChatMessage saveAssistantMessage(UUID sessionId, String content,
                                                   String requestId,
                                                   ChatMessageRepository chatMessageRepository) {
        return saveAssistantMessage(sessionId, content, requestId, null, chatMessageRepository);
    }

    public static ChatMessage saveAssistantMessage(UUID sessionId, String content,
                                                   String requestId,
                                                   Map<String, Object> metadata,
                                                   ChatMessageRepository chatMessageRepository) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_ASSISTANT);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        message.setTokenCount(estimateTokens(content));
        message.setRequestId(requestId);
        message.setMetadata(metadata != null ? new LinkedHashMap<>(metadata) : null);
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        try {
            return chatMessageRepository.save(message);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            if (requestId != null) {
                return chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                        sessionId, AIChatConstants.ROLE_ASSISTANT, requestId);
            }
            throw dup;
        }
    }

    /**
     * 保存系统消息（可带 requestId，用于幂等）
     *
     * @param sessionId             会话ID
     * @param content               消息内容
     * @param requestId             请求ID（用于幂等）
     * @param chatMessageRepository 消息仓库
     * @return 保存的系统消息
     */
    public static ChatMessage saveSystemMessage(UUID sessionId, String content,
                                                String requestId,
                                                ChatMessageRepository chatMessageRepository) {
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_SYSTEM);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setRequestId(requestId);
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        try {
            return chatMessageRepository.save(message);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            if (requestId != null) {
                return chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                        sessionId, AIChatConstants.ROLE_SYSTEM, requestId);
            }
            throw dup;
        }
    }

    /**
     * 仅当与最后一条用户消息不相同时才保存；若提供 requestId 则走幂等插入
     *
     * @param sessionId              会话ID
     * @param content                消息内容
     * @param attachments            附件列表
     * @param fileReferences         引用文件列表
     * @param lastMessageFromContext 上下文中的最后一条消息
     * @param requestId              请求ID（用于幂等性）
     * @param chatMessageRepository  消息仓库
     * @return 保存或已存在的用户消息
     */
    public static ChatMessage addUserMessageIfNotDuplicate(UUID sessionId, String content,
                                                           List<String> attachments,
                                                           List<FileReference> fileReferences,
                                                           ChatMessage lastMessageFromContext,
                                                           String requestId,
                                                           ChatMessageRepository chatMessageRepository) {
        if (requestId != null) {
            // 先根据 requestId 查询是否已经存在对应的用户消息，存在则直接返回，避免重复写入
            ChatMessage existing = chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                    sessionId, AIChatConstants.ROLE_USER, requestId);
            if (existing != null) {
                return existing;
            }
            // 不存在再尝试插入，数据库唯一索引作为第二道幂等防线
            return addUserMessage(sessionId, content, attachments, fileReferences, requestId, chatMessageRepository);
        }
        if (lastMessageFromContext != null
                && Objects.equals(lastMessageFromContext.getRole(), AIChatConstants.ROLE_USER)
                && Objects.equals(lastMessageFromContext.getContent(), content)
                && attachmentsEqual(lastMessageFromContext.getAttachments(), attachments)
                && equalFileReferences(lastMessageFromContext.getFileReferences(), fileReferences)) {
            return lastMessageFromContext;
        }
        return addUserMessage(sessionId, content, attachments, fileReferences, null, chatMessageRepository);
    }

    /**
     * 上下文返回体：模型消息列表 + 最后一条历史消息
     */
    public static boolean isResendRequest(ChatRequestDTO request) {
        return request != null && (request.getResendSourceUserMessageId() != null
                || request.getResendSourceAssistantMessageId() != null
                || StringUtils.hasText(request.getResendSourceRequestId()));
    }

    public static boolean isResendRequest(KafkaChatRequestDTO request) {
        return request != null && (request.getResendSourceUserMessageId() != null
                || request.getResendSourceAssistantMessageId() != null
                || StringUtils.hasText(request.getResendSourceRequestId()));
    }

    public static ChatMessage resolveResendSourceUserMessage(UUID sessionId,
                                                             UUID sourceUserMessageId,
                                                             String sourceRequestId,
                                                             ChatMessageRepository chatMessageRepository) {
        if (sessionId == null || chatMessageRepository == null) {
            return null;
        }
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        return resolveResendSourceUserMessage(history, sourceUserMessageId, sourceRequestId);
    }

    public static String resolveResponseVariantGroupId(ChatMessage sourceUserMessage) {
        if (sourceUserMessage == null) {
            return null;
        }
        if (StringUtils.hasText(sourceUserMessage.getRequestId())) {
            return sourceUserMessage.getRequestId();
        }
        return sourceUserMessage.getId() != null ? sourceUserMessage.getId().toString() : null;
    }

    public static int resolveResponseVariantIndex(UUID sessionId,
                                                  UUID sourceUserMessageId,
                                                  String sourceRequestId,
                                                  ChatMessageRepository chatMessageRepository) {
        if (sessionId == null || chatMessageRepository == null) {
            return 0;
        }
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        ChatMessage sourceUserMessage = resolveResendSourceUserMessage(history, sourceUserMessageId, sourceRequestId);
        if (sourceUserMessage == null || sourceUserMessage.getId() == null) {
            return 0;
        }
        int sourceIndex = indexOfMessage(history, sourceUserMessage.getId());
        if (sourceIndex < 0) {
            return 0;
        }
        int variantCount = 0;
        for (int i = sourceIndex + 1; i < history.size(); i++) {
            ChatMessage current = history.get(i);
            if (Objects.equals(current.getRole(), AIChatConstants.ROLE_USER)) {
                break;
            }
            if (Objects.equals(current.getRole(), AIChatConstants.ROLE_ASSISTANT)) {
                variantCount++;
            }
        }
        return variantCount;
    }

    public static Map<String, Object> buildResponseVariantMetadata(String groupId,
                                                                   Integer variantIndex,
                                                                   ChatMessage sourceUserMessage,
                                                                   UUID sourceAssistantMessageId,
                                                                   String sourceRequestId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        if (StringUtils.hasText(groupId)) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_GROUP_ID, groupId);
        }
        if (variantIndex != null) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_INDEX, variantIndex);
        }
        if (sourceUserMessage != null && sourceUserMessage.getId() != null) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_SOURCE_USER_MESSAGE_ID,
                    sourceUserMessage.getId().toString());
        }
        if (sourceAssistantMessageId != null) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_SOURCE_ASSISTANT_MESSAGE_ID,
                    sourceAssistantMessageId.toString());
        }
        String resolvedSourceRequestId = StringUtils.hasText(sourceRequestId)
                ? sourceRequestId
                : sourceUserMessage != null ? sourceUserMessage.getRequestId() : null;
        if (StringUtils.hasText(resolvedSourceRequestId)) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_SOURCE_REQUEST_ID, resolvedSourceRequestId);
        }
        return metadata.isEmpty() ? null : metadata;
    }

    public static Map<String, Object> buildResponseVariantMetadata(UUID sessionId,
                                                                   UUID sourceUserMessageId,
                                                                   UUID sourceAssistantMessageId,
                                                                   String sourceRequestId,
                                                                   ChatMessage persistedUserMessage,
                                                                   ChatMessageRepository chatMessageRepository) {
        ChatMessage sourceUserMessage = persistedUserMessage != null
                ? persistedUserMessage
                : resolveResendSourceUserMessage(sessionId, sourceUserMessageId, sourceRequestId, chatMessageRepository);
        String groupId = resolveResponseVariantGroupId(sourceUserMessage);
        if (!StringUtils.hasText(groupId)) {
            groupId = sourceRequestId;
        }
        int variantIndex = persistedUserMessage != null
                ? 0
                : resolveResponseVariantIndex(sessionId, sourceUserMessageId, sourceRequestId, chatMessageRepository);
        return buildResponseVariantMetadata(groupId, variantIndex, sourceUserMessage, sourceAssistantMessageId, sourceRequestId);
    }

    private static Map<String, Object> buildUserVariantMetadata(UUID userMessageId, String requestId) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        String groupId = StringUtils.hasText(requestId)
                ? requestId
                : userMessageId != null ? userMessageId.toString() : null;
        if (StringUtils.hasText(groupId)) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_GROUP_ID, groupId);
        }
        if (StringUtils.hasText(requestId)) {
            metadata.put(AIChatConstants.METADATA_RESPONSE_VARIANT_SOURCE_REQUEST_ID, requestId);
        }
        return metadata.isEmpty() ? null : metadata;
    }

    private static ChatMessage resolveResendSourceUserMessage(List<ChatMessage> history,
                                                              UUID sourceUserMessageId,
                                                              String sourceRequestId) {
        if (history == null || history.isEmpty()) {
            return null;
        }
        if (sourceUserMessageId != null) {
            for (ChatMessage current : history) {
                if (Objects.equals(current.getRole(), AIChatConstants.ROLE_USER)
                        && Objects.equals(current.getId(), sourceUserMessageId)) {
                    return current;
                }
            }
        }
        if (!StringUtils.hasText(sourceRequestId)) {
            return null;
        }
        for (ChatMessage current : history) {
            if (Objects.equals(current.getRole(), AIChatConstants.ROLE_USER)
                    && Objects.equals(current.getRequestId(), sourceRequestId)) {
                return current;
            }
        }
        return null;
    }

    private static List<ChatMessage> trimHistoryBeforeSourceMessage(List<ChatMessage> history,
                                                                    ChatMessage sourceUserMessage) {
        if (history == null || history.isEmpty() || sourceUserMessage == null || sourceUserMessage.getId() == null) {
            return history;
        }
        int sourceIndex = indexOfMessage(history, sourceUserMessage.getId());
        if (sourceIndex < 0) {
            return history;
        }
        if (sourceIndex == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history.subList(0, sourceIndex));
    }

    private static int indexOfMessage(List<ChatMessage> history, UUID messageId) {
        if (history == null || history.isEmpty() || messageId == null) {
            return -1;
        }
        for (int i = 0; i < history.size(); i++) {
            if (Objects.equals(history.get(i).getId(), messageId)) {
                return i;
            }
        }
        return -1;
    }

    public record ChatContext(List<Message> messages, ChatMessage lastMessage) {
    }
}

