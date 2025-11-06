package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatSessionQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IChatSessionService {

    PageInfo<ChatSessionVO> listChatSession(ChatSessionQueryDTO chatSessionQueryDTO);

    List<ChatSessionVO> listAllChatSessionByUserId(UUID sysUserId);

    ChatSessionVO getChatSessionById(UUID id);

    ChatSessionVO addChatSession(UUID courseId, Integer sessionType, String title);

    Boolean updateChatSessionTitle(UUID id, String title);

    Boolean removeChatSessionById(UUID id);

    Integer removeChatSessionByIds(List<UUID> ids);

    Boolean pinSession(UUID id, Boolean isPinned);

    Boolean favoriteSession(UUID id, Boolean isFavorite);

    Boolean archiveSession(UUID id);

    Boolean updateSessionLastMessage(UUID sessionId, String lastMessage);
}

