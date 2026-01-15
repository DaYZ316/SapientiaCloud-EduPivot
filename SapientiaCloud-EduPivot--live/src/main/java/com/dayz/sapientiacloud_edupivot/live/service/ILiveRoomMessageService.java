package com.dayz.sapientiacloud_edupivot.live.service;

import com.dayz.sapientiacloud_edupivot.live.entity.po.LiveRoomMessage;

import java.util.List;
import java.util.UUID;

public interface ILiveRoomMessageService {

    LiveRoomMessage appendMessage(UUID liveRoomId,
                                  UUID senderId,
                                  String senderName,
                                  Integer senderRole,
                                  String content,
                                  String messageType);

    List<LiveRoomMessage> listLatestMessages(UUID liveRoomId, int limit);
}


