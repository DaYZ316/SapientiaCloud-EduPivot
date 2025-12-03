package com.dayz.sapientiacloud_edupivot.live.room.service.impl;

import com.dayz.sapientiacloud_edupivot.live.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.room.entity.po.LiveRoomMessage;
import com.dayz.sapientiacloud_edupivot.live.room.repository.LiveRoomMessageRepository;
import com.dayz.sapientiacloud_edupivot.live.room.service.ILiveRoomMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveRoomMessageServiceImpl implements ILiveRoomMessageService {

    private static final int DEFAULT_LIMIT = 50;

    private final LiveRoomMessageRepository liveRoomMessageRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoomMessage appendMessage(UUID liveRoomId,
                                         UUID senderId,
                                         String senderName,
                                         Integer senderRole,
                                         String content,
                                         String messageType) {
        if (liveRoomId == null || senderId == null) {
            throw new BusinessException(ResultEnum.PARAM_ERROR);
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ResultEnum.PARAM_ERROR);
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
        return liveRoomMessageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveRoomMessage> listLatestMessages(UUID liveRoomId, int limit) {
        if (liveRoomId == null) {
            throw new BusinessException(ResultEnum.PARAM_ERROR);
        }
        int pageSize = limit > 0 ? limit : DEFAULT_LIMIT;
        Pageable pageable = PageRequest.of(0, pageSize);
        return liveRoomMessageRepository.findByLiveRoomIdOrderBySendTimeDesc(liveRoomId, pageable);
    }
}


