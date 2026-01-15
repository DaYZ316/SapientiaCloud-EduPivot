package com.dayz.sapientiacloud_edupivot.live.repository;

import com.dayz.sapientiacloud_edupivot.live.entity.po.LiveRoomMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LiveRoomMessageRepository extends MongoRepository<LiveRoomMessage, UUID> {

    List<LiveRoomMessage> findByLiveRoomIdOrderBySendTimeDesc(UUID liveRoomId, Pageable pageable);
}


