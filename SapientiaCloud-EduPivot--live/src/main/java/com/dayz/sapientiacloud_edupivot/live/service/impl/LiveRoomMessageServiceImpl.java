package com.dayz.sapientiacloud_edupivot.live.service.impl;

import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.entity.po.LiveRoomMessage;
import com.dayz.sapientiacloud_edupivot.live.enums.LiveRoomEnum;
import com.dayz.sapientiacloud_edupivot.live.repository.LiveRoomMessageRepository;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomMessageService;
import com.dayz.sapientiacloud_edupivot.live.event.LiveEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveRoomMessageServiceImpl implements ILiveRoomMessageService {

    private static final int DEFAULT_LIMIT = 50;

    private final LiveRoomMessageRepository liveRoomMessageRepository;
    private final LiveEventPublisher liveEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomMessage appendMessage(UUID liveRoomId,
                                         UUID senderId,
                                         String senderName,
                                         Integer senderRole,
                                         String content,
                                         String messageType) {
        if (liveRoomId == null) {
            throw new BusinessException(LiveRoomEnum.LIVE_ROOM_ID_REQUIRED);
        }
        if (senderId == null) {
            throw new BusinessException(LiveRoomEnum.SENDER_ID_REQUIRED);
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(LiveRoomEnum.MESSAGE_CONTENT_REQUIRED);
        }
        LiveRoomMessage message = new LiveRoomMessage();
        message.setId(UUID.randomUUID());
        message.setLiveRoomId(liveRoomId);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setMessageType(StringUtils.hasText(messageType) ? messageType : "text");
        message.setSendTime(LocalDateTime.now());
        LiveRoomMessage savedMessage = liveRoomMessageRepository.save(message);

        // 广播消息到SSE客户端
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", "chat_message");
            payload.put("roomId", liveRoomId.toString());

            Map<String, Object> data = new HashMap<>();
            data.put("id", savedMessage.getId().toString());
            data.put("sender", savedMessage.getSenderName());
            data.put("content", savedMessage.getContent());
            data.put("sendTime", savedMessage.getSendTime().toString());
            data.put("messageType", savedMessage.getMessageType());
            payload.put("data", data);

            liveEventPublisher.publishToClassroom(liveRoomId.toString(), payload);
        } catch (Exception e) {
            // 广播失败不影响消息保存
        }

        return savedMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveRoomMessage> listLatestMessages(UUID liveRoomId, int limit) {
        if (liveRoomId == null) {
            throw new BusinessException(LiveRoomEnum.LIVE_ROOM_ID_REQUIRED);
        }
        int pageSize = limit > 0 ? limit : DEFAULT_LIMIT;
        Pageable pageable = PageRequest.of(0, pageSize);
        return liveRoomMessageRepository.findByLiveRoomIdOrderBySendTimeDesc(liveRoomId, pageable);
    }
}


