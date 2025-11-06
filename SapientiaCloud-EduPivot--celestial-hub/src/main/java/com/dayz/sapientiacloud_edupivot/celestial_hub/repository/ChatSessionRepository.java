package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * AI对话会话Repository
 * 注意：所有查询操作通过 MongoTemplate 实现动态查询，不需要预定义方法
 */
@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, UUID> {
}

