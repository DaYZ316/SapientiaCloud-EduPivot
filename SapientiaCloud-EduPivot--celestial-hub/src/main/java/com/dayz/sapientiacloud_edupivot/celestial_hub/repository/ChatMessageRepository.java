package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * AI对话消息Repository
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, UUID> {

    /**
     * 根据会话ID查询消息（分页）
     */
    Page<ChatMessage> findBySessionIdOrderByCreateTimeAsc(UUID sessionId, Pageable pageable);

    /**
     * 根据会话ID查询所有消息
     */
    List<ChatMessage> findBySessionIdOrderByCreateTimeAsc(UUID sessionId);

    /**
     * 根据会话ID统计消息数量
     */
    long countBySessionId(UUID sessionId);

    /**
     * 根据会话ID查询最后一条消息
     */
    ChatMessage findFirstBySessionIdOrderByCreateTimeDesc(UUID sessionId);

    /**
     * 删除会话的所有消息
     */
    void deleteBySessionId(UUID sessionId);
}

