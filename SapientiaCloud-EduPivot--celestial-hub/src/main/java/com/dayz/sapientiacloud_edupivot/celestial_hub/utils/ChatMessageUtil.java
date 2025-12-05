package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KafkaChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchResultVO;
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
import java.util.List;
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
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));

        // 获取历史消息
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        ChatMessage last = history.isEmpty() ? null : history.get(history.size() - 1);
        for (ChatMessage msg : history) {
            if (AIChatConstants.ROLE_USER.equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_ASSISTANT.equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_SYSTEM.equals(msg.getRole())) {
                messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // 只有当最后一条历史消息不是同一条用户消息时才追加当前请求
        if (!isSameAsLastUserMessage(last, request.getMessage(), request.getAttachments(), request.getFileReferences())) {
            messages.add(new UserMessage(request.getMessage()));
        }
        return new ChatContext(messages, last);
    }

    /**
     * 构建消息上下文（Kafka版本，返回上下文消息与最后一条历史消息）
     */
    public static ChatContext buildContext(UUID sessionId, KafkaChatRequestDTO request, ChatMessageRepository chatMessageRepository) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(AIChatConstants.SYSTEM_PROMPT));

        // 获取历史消息
        List<ChatMessage> history = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(sessionId);
        ChatMessage last = history.isEmpty() ? null : history.get(history.size() - 1);
        for (ChatMessage msg : history) {
            if (AIChatConstants.ROLE_USER.equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_ASSISTANT.equals(msg.getRole())) {
                messages.add(new AssistantMessage(msg.getContent()));
            } else if (AIChatConstants.ROLE_SYSTEM.equals(msg.getRole())) {
                messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // 只有当最后一条历史消息不是同一条用户消息时才追加当前请求
        if (!isSameAsLastUserMessage(last, request.getMessage(), request.getAttachments(), request.getFileReferences())) {
            messages.add(new UserMessage(request.getMessage()));
        }
        return new ChatContext(messages, last);
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
        message.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
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
        return saveAssistantMessage(sessionId, content, null, chatMessageRepository);
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
        ChatMessage message = new ChatMessage();
        message.setId(UuidCreator.getTimeOrderedEpoch());
        message.setSessionId(sessionId);
        message.setRole(AIChatConstants.ROLE_ASSISTANT);
        message.setContent(content);
        message.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        message.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        message.setTokenCount(estimateTokens(content));
        message.setRequestId(requestId);
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
    public record ChatContext(List<Message> messages, ChatMessage lastMessage) {
    }
}

