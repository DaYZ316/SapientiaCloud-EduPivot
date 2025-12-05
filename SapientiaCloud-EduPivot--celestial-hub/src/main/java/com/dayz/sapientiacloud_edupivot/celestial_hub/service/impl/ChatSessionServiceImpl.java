package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.FileDocumentConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatSessionQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatSession;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatSessionRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements IChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MongoTemplate mongoTemplate;
    private final ChatClient chatClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<ChatSessionVO> listChatSession(ChatSessionQueryDTO chatSessionQueryDTO) {
        if (chatSessionQueryDTO == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (chatSessionQueryDTO.getSysUserId() != null) {
            criteria.and(AIChatConstants.FIELD_SYS_USER_ID).is(chatSessionQueryDTO.getSysUserId());
        }

        if (StringUtils.hasText(chatSessionQueryDTO.getSessionTitle())) {
            criteria.and(AIChatConstants.FIELD_SESSION_TITLE).regex(chatSessionQueryDTO.getSessionTitle(),
                    FileDocumentConstants.REGEX_CASE_INSENSITIVE);
        }

        if (chatSessionQueryDTO.getSessionType() != null) {
            criteria.and(AIChatConstants.FIELD_SESSION_TYPE).is(chatSessionQueryDTO.getSessionType());
        }

        if (chatSessionQueryDTO.getIsPinned() != null) {
            criteria.and(AIChatConstants.FIELD_IS_PINNED).is(chatSessionQueryDTO.getIsPinned());
        }

        if (chatSessionQueryDTO.getIsFavorite() != null) {
            criteria.and(AIChatConstants.FIELD_IS_FAVORITE).is(chatSessionQueryDTO.getIsFavorite());
        }

        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(AIChatConstants.FIELD_IS_PINNED),
                Sort.Order.desc(AIChatConstants.FIELD_UPDATE_TIME)
        ));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                chatSessionQueryDTO.getPageNum() - AIChatConstants.PAGE_NUM_OFFSET,
                chatSessionQueryDTO.getPageSize()
        );

        query.with(pageable);

        long total = mongoTemplate.count(countQuery, ChatSession.class);
        List<ChatSession> sessions = mongoTemplate.find(query, ChatSession.class);

        List<ChatSessionVO> chatSessionVOList = batchConvertToVO(sessions);

        PageInfo<ChatSessionVO> pageInfo = new PageInfo<>();
        pageInfo.setList(chatSessionVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(chatSessionQueryDTO.getPageNum());
        pageInfo.setPageSize(chatSessionQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / chatSessionQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatSessionVO> listAllChatSessionByUserId(UUID sysUserId) {
        if (sysUserId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }

        Query query = new Query();
        query.addCriteria(Criteria.where(AIChatConstants.FIELD_SYS_USER_ID).is(sysUserId));
        query.with(Sort.by(
                Sort.Order.desc(AIChatConstants.FIELD_IS_PINNED),
                Sort.Order.desc(AIChatConstants.FIELD_UPDATE_TIME)
        ));

        List<ChatSession> sessions = mongoTemplate.find(query, ChatSession.class);
        return batchConvertToVO(sessions);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatSessionVO getChatSessionById(UUID id) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        return convertToVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO addChatSession(UUID courseId, Integer sessionType, String title) {
        return addChatSession(UserContextUtil.getCurrentUserId(), courseId, sessionType, title);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionVO addChatSession(UUID userId, UUID courseId, Integer sessionType, String title) {
        if (userId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }
        ChatSession session = new ChatSession();
        session.setId(UuidCreator.getTimeOrderedEpoch());
        session.setSysUserId(userId);
        session.setSessionTitle(StringUtils.hasText(title) ? title : AIChatConstants.DEFAULT_SESSION_TITLE);
        session.setSessionType(sessionType != null ? sessionType : SessionTypeEnum.GENERAL.getCode());
        session.setIsPinned(AIChatConstants.PINNED_FALSE);
        session.setIsFavorite(AIChatConstants.FAVORITE_FALSE);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());

        ChatSession savedSession = chatSessionRepository.save(session);
        return convertToVO(savedSession);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateChatSessionTitle(UUID id, String title) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        if (!StringUtils.hasText(title)) {
            throw new BusinessException(AIChatEnum.MESSAGE_CONTENT_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        session.setSessionTitle(title);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionRepository.save(session);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeChatSessionById(UUID id) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        chatSessionRepository.delete(session);
        chatMessageRepository.deleteBySessionId(id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer removeChatSessionByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return AIChatConstants.DEFAULT_MESSAGE_COUNT;
        }

        Query query = new Query();
        query.addCriteria(Criteria.where(AIChatConstants.FIELD_ID).in(ids));
        List<ChatSession> sessions = mongoTemplate.find(query, ChatSession.class);

        if (sessions.isEmpty()) {
            return AIChatConstants.DEFAULT_MESSAGE_COUNT;
        }

        chatSessionRepository.deleteAll(sessions);

        List<UUID> sessionIds = sessions.stream()
                .map(ChatSession::getId)
                .toList();

        Query deleteMessageQuery = new Query();
        deleteMessageQuery.addCriteria(Criteria.where(AIChatConstants.FIELD_SESSION_ID).in(sessionIds));
        mongoTemplate.remove(deleteMessageQuery, ChatMessage.class);

        return sessions.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pinSession(UUID id, Boolean isPinned) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        session.setIsPinned(Boolean.TRUE.equals(isPinned) ?
                AIChatConstants.PINNED_TRUE : AIChatConstants.PINNED_FALSE);
        chatSessionRepository.save(session);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteSession(UUID id, Boolean isFavorite) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        session.setIsFavorite(Boolean.TRUE.equals(isFavorite) ?
                AIChatConstants.FAVORITE_TRUE : AIChatConstants.FAVORITE_FALSE);
        chatSessionRepository.save(session);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean archiveSession(UUID id) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        session.setUpdateTime(LocalDateTime.now());
        chatSessionRepository.save(session);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSessionLastMessage(UUID sessionId, String lastMessage) {
        if (sessionId == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        if (AIChatConstants.DEFAULT_SESSION_TITLE.equals(session.getSessionTitle()) &&
                StringUtils.hasText(lastMessage)) {
            String title = lastMessage.length() > AIChatConstants.DEFAULT_TITLE_MAX_LENGTH ?
                    lastMessage.substring(AIChatConstants.DEFAULT_MESSAGE_COUNT,
                            AIChatConstants.DEFAULT_TITLE_MAX_LENGTH) + AIChatConstants.ELLIPSIS
                    : lastMessage;
            session.setSessionTitle(title);
        }

        session.setUpdateTime(LocalDateTime.now());
        chatSessionRepository.save(session);

        return true;
    }

    private List<ChatSessionVO> batchConvertToVO(List<ChatSession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> sessionIds = sessions.stream()
                .map(ChatSession::getId)
                .toList();

        // 使用聚合查询获取每个会话的最后一条消息（优化：只返回每个会话的最新消息）
        // 如果 sessionIds 数量较少，可以为每个单独查询；否则使用聚合查询
        Map<UUID, ChatMessage> lastMessageMap = new HashMap<>();
        if (sessionIds.size() <= 10) {
            // 数量少时，直接查询每个会话的最后一条消息
            for (UUID sessionId : sessionIds) {
                ChatMessage lastMessage = chatMessageRepository
                        .findFirstBySessionIdOrderByCreateTimeDesc(sessionId);
                if (lastMessage != null) {
                    lastMessageMap.put(sessionId, lastMessage);
                }
            }
        } else {
            // 数量多时，使用聚合查询优化
            MatchOperation matchLastMessage = Aggregation.match(
                    Criteria.where(AIChatConstants.FIELD_SESSION_ID).in(sessionIds));
            SortOperation sortByCreateTime = Aggregation.sort(Sort.Direction.DESC, AIChatConstants.FIELD_CREATE_TIME);
            GroupOperation groupBySession = Aggregation.group(AIChatConstants.FIELD_SESSION_ID)
                    .first(AIChatConstants.FIELD_ID).as("messageId")
                    .first("content").as("content")
                    .first(AIChatConstants.FIELD_CREATE_TIME).as("createTime");
            Aggregation lastMessageAggregation = Aggregation.newAggregation(
                    matchLastMessage, sortByCreateTime, groupBySession);

            var lastMessageResults = mongoTemplate.aggregate(
                    lastMessageAggregation, AIChatConstants.COLLECTION_CHAT_MESSAGE, Map.class);

            // 从聚合结果中提取消息ID，然后批量查询完整消息对象
            List<UUID> lastMessageIds = new ArrayList<>();
            HashMap<UUID, UUID> sessionIdToMessageIdMap = new HashMap<>();
            lastMessageResults.getMappedResults().forEach(result -> {
                Object sessionIdObj = result.get(AIChatConstants.FIELD_ID);
                Object messageIdObj = result.get("messageId");
                if (sessionIdObj != null && messageIdObj != null) {
                    UUID sessionId = sessionIdObj instanceof UUID ? (UUID) sessionIdObj :
                            UUID.fromString(sessionIdObj.toString());
                    UUID messageId = messageIdObj instanceof UUID ? (UUID) messageIdObj :
                            UUID.fromString(messageIdObj.toString());
                    sessionIdToMessageIdMap.put(sessionId, messageId);
                    lastMessageIds.add(messageId);
                }
            });

            // 批量查询消息对象
            if (!lastMessageIds.isEmpty()) {
                Query messageQuery = new Query();
                messageQuery.addCriteria(Criteria.where(AIChatConstants.FIELD_ID).in(lastMessageIds));
                List<ChatMessage> lastMessages = mongoTemplate.find(messageQuery, ChatMessage.class);
                Map<UUID, ChatMessage> messageMap = lastMessages.stream()
                        .collect(Collectors.toMap(ChatMessage::getId, msg -> msg));
                for (Map.Entry<UUID, UUID> entry : sessionIdToMessageIdMap.entrySet()) {
                    ChatMessage msg = messageMap.get(entry.getValue());
                    if (msg != null) {
                        lastMessageMap.put(entry.getKey(), msg);
                    }
                }
            }
        }

        // 使用聚合查询统计每个会话的消息数量
        MatchOperation matchCount = Aggregation.match(
                Criteria.where(AIChatConstants.FIELD_SESSION_ID).in(sessionIds));
        GroupOperation groupCount = Aggregation.group(AIChatConstants.FIELD_SESSION_ID)
                .count().as(AIChatConstants.FIELD_COUNT);
        Aggregation countAggregation = Aggregation.newAggregation(matchCount, groupCount);

        var countResults = mongoTemplate.aggregate(
                countAggregation, AIChatConstants.COLLECTION_CHAT_MESSAGE, Map.class);

        HashMap<UUID, Long> messageCountMap = new HashMap<>();
        countResults.getMappedResults().forEach(result -> {
            Object sessionIdObj = result.get(AIChatConstants.FIELD_ID);
            Object countObj = result.get(AIChatConstants.FIELD_COUNT);
            if (sessionIdObj != null && countObj != null) {
                UUID sessionId = sessionIdObj instanceof UUID ? (UUID) sessionIdObj :
                        UUID.fromString(sessionIdObj.toString());
                Long count = countObj instanceof Number ? ((Number) countObj).longValue() :
                        Long.parseLong(countObj.toString());
                messageCountMap.put(sessionId, count);
            }
        });

        return sessions.stream()
                .map(session -> convertToVO(session, lastMessageMap.get(session.getId()),
                        messageCountMap.get(session.getId())))
                .toList();
    }

    private ChatSessionVO convertToVO(ChatSession session) {
        ChatMessage lastMessage = chatMessageRepository
                .findFirstBySessionIdOrderByCreateTimeDesc(session.getId());
        long messageCount = chatMessageRepository.countBySessionId(session.getId());
        return convertToVO(session, lastMessage, messageCount);
    }

    /**
     * 将 ChatSession 转换为 ChatSessionVO（提取公共逻辑）
     */
    private ChatSessionVO convertToVO(ChatSession session, ChatMessage lastMessage, Long messageCount) {
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);

        if (lastMessage != null) {
            String preview = lastMessage.getContent();
            vo.setLastMessagePreview(preview != null &&
                    preview.length() > AIChatConstants.DEFAULT_PREVIEW_MAX_LENGTH ?
                    preview.substring(AIChatConstants.DEFAULT_MESSAGE_COUNT,
                            AIChatConstants.DEFAULT_PREVIEW_MAX_LENGTH) + AIChatConstants.ELLIPSIS
                    : preview);
        }

        vo.setMessageCount(messageCount != null ? messageCount.intValue() : AIChatConstants.DEFAULT_MESSAGE_COUNT);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateSessionTitle(UUID id) {
        if (id == null) {
            throw new BusinessException(AIChatEnum.SESSION_ID_REQUIRED);
        }

        ChatSession session = chatSessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(AIChatEnum.SESSION_NOT_EXISTS));

        // 获取会话的前几条消息用于生成标题
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreateTimeAsc(id);
        if (messages == null || messages.isEmpty()) {
            throw new BusinessException(AIChatEnum.SESSION_NO_MESSAGES);
        }

        // 只取前5条消息用于生成标题
        int messageLimit = Math.min(5, messages.size());
        List<ChatMessage> recentMessages = messages.subList(0, messageLimit);

        // 构建对话内容文本
        StringBuilder conversationText = new StringBuilder();
        for (ChatMessage message : recentMessages) {
            String role = Objects.equals(message.getRole(), AIChatConstants.ROLE_USER) ? "用户" : "助手";
            String content = message.getContent();
            if (StringUtils.hasText(content)) {
                conversationText.append(role).append(": ").append(content).append("\n");
            }
        }

        if (!StringUtils.hasText(conversationText.toString())) {
            throw new BusinessException(AIChatEnum.SESSION_NO_MESSAGES);
        }

        // 构建AI提示词
        String prompt = String.format(
                "请根据以下对话内容，生成一个简洁的标题（10-20字，不要包含引号、冒号等标点符号）：\n\n%s\n\n请只返回标题，不要包含其他内容。",
                conversationText.toString()
        );

        try {
            // 调用AI生成标题
            String generatedTitle = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            // 清理生成的标题（移除可能的引号、换行等）
            if (StringUtils.hasText(generatedTitle)) {
                generatedTitle = generatedTitle.trim()
                        .replace("\"", "")
                        .replace("'", "")
                        .replace("\n", "")
                        .replace("\r", "");

                // 限制标题长度
                if (generatedTitle.length() > AIChatConstants.DEFAULT_TITLE_MAX_LENGTH) {
                    generatedTitle = generatedTitle.substring(0, AIChatConstants.DEFAULT_TITLE_MAX_LENGTH);
                }

                // 更新会话标题
                session.setSessionTitle(generatedTitle);
                session.setUpdateTime(LocalDateTime.now());
                chatSessionRepository.save(session);

                return generatedTitle;
            } else {
                log.error("AI生成标题返回空内容: sessionId={}", id);
                throw new BusinessException(AIChatEnum.AI_SERVICE_ERROR);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI生成标题失败: sessionId={}, error={}", id, e.getMessage(), e);
            throw new BusinessException(AIChatEnum.AI_SERVICE_ERROR);
        }
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void generateSessionTitleAsync(UUID id) {
        try {
            generateSessionTitle(id);
            log.info("异步生成会话标题成功: sessionId={}", id);
        } catch (Exception e) {
            log.error("异步生成会话标题失败: sessionId={}, error={}", id, e.getMessage(), e);
            // 异步方法中不抛出异常，只记录日志
        }
    }
}

