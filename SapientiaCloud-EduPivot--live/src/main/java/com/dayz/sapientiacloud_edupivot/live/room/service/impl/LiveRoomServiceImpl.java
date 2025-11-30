package com.dayz.sapientiacloud_edupivot.live.room.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dayz.sapientiacloud_edupivot.live.common.config.LiveKitProperties;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoomUser;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.room.mapper.LiveRoomMapper;
import com.dayz.sapientiacloud_edupivot.live.room.mapper.LiveRoomUserMapper;
import com.dayz.sapientiacloud_edupivot.live.room.service.ILiveRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class LiveRoomServiceImpl implements ILiveRoomService {

    private final LiveRoomMapper liveRoomMapper;
    private final LiveRoomUserMapper liveRoomUserMapper;
    private final LiveKitProperties liveKitProperties;

    public LiveRoomServiceImpl(LiveRoomMapper liveRoomMapper, LiveRoomUserMapper liveRoomUserMapper, LiveKitProperties liveKitProperties) {
        this.liveRoomMapper = liveRoomMapper;
        this.liveRoomUserMapper = liveRoomUserMapper;
        this.liveKitProperties = liveKitProperties;
    }

    @Override
    @Transactional
    public LiveRoom createRoom(String roomName, UUID creatorId, UUID courseId, UUID classroomId, Integer maxParticipants, Integer recordingEnabled) {
        LiveRoom room = new LiveRoom();
        room.setId(UUID.randomUUID());
        room.setRoomName(roomName);
        room.setCreatorId(creatorId);
        room.setCourseId(courseId);
        room.setClassroomId(classroomId);
        room.setStatus(0);
        String lkRoomName = buildLkRoomName(roomName, classroomId);
        room.setLkRoomName(lkRoomName);
        room.setMaxParticipants(Objects.requireNonNullElse(maxParticipants, defaultMaxParticipants()));
        room.setRecordingEnabled(Objects.requireNonNullElse(recordingEnabled, 0));
        room.setExpectedEndTime(null);
        liveRoomMapper.insert(room);
        return room;
    }

    @Override
    @Transactional
    public void closeRoom(UUID roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        room.setStatus(2);
        room.setEndTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);
    }

    @Override
    @Transactional
    public String issueToken(UUID roomId, UUID userId, String username, Integer role) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        if (room.getStatus() == 0) {
            room.setStatus(1);
            room.setStartTime(LocalDateTime.now());
            liveRoomMapper.updateById(room);
        }
        String identity = userId + "-" + UUID.randomUUID();
        boolean canPublish = role != null && role == 1;
        String token = buildLiveKitAccessToken(room.getLkRoomName(), identity, username, canPublish);

        LiveRoomUser liveRoomUser = new LiveRoomUser();
        liveRoomUser.setId(UUID.randomUUID());
        liveRoomUser.setLiveRoomId(roomId);
        liveRoomUser.setUserId(userId);
        liveRoomUser.setRole(Objects.requireNonNullElse(role, 0));
        liveRoomUser.setJoinTime(LocalDateTime.now());
        liveRoomUser.setLkIdentity(identity);
        liveRoomUser.setTokenJti(extractJti(token));

        LambdaQueryWrapper<LiveRoomUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveRoomUser::getLiveRoomId, roomId)
                .eq(LiveRoomUser::getUserId, userId)
                .isNull(LiveRoomUser::getLeaveTime)
                .last("limit 1");
        LiveRoomUser existing = liveRoomUserMapper.selectOne(wrapper);
        if (existing == null) {
            liveRoomUserMapper.insert(liveRoomUser);
        }

        return token;
    }

    @Override
    public LiveRoom getById(UUID id) {
        return liveRoomMapper.selectById(id);
    }

    @Override
    public java.util.List<LiveRoom> listRooms(Integer status, UUID courseId, UUID classroomId) {
        LambdaQueryWrapper<LiveRoom> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LiveRoom::getStatus, status);
        }
        if (courseId != null) {
            wrapper.eq(LiveRoom::getCourseId, courseId);
        }
        if (classroomId != null) {
            wrapper.eq(LiveRoom::getClassroomId, classroomId);
        }
        wrapper.orderByDesc(LiveRoom::getStartTime);
        return liveRoomMapper.selectList(wrapper);
    }

    private String buildLkRoomName(String roomName, UUID classroomId) {
        if (classroomId != null) {
            return "cls-" + classroomId + "-" + roomName;
        }
        return "room-" + UUID.randomUUID();
    }

    private int defaultMaxParticipants() {
        LiveKitProperties.RoomDefaults roomDefaults = liveKitProperties.getRoom();
        if (roomDefaults != null && roomDefaults.getMaxParticipants() != null) {
            return roomDefaults.getMaxParticipants();
        }
        return 500;
    }

    private String buildLiveKitAccessToken(String roomName, String identity, String displayName, boolean canPublish) {
        Algorithm algorithm = Algorithm.HMAC256(liveKitProperties.getApiSecret());
        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("roomJoin", true);
        videoGrant.put("room", roomName);
        videoGrant.put("canPublish", canPublish);
        videoGrant.put("canSubscribe", true);
        videoGrant.put("canPublishData", true);

        Instant now = Instant.now();
        int ttl = Objects.requireNonNullElse(liveKitProperties.getTokenTtlSeconds(), 7200);
        Instant exp = now.plusSeconds(ttl);

        return JWT.create()
                .withIssuer(liveKitProperties.getApiKey())
                .withSubject(identity)
                .withClaim("name", displayName)
                .withClaim("video", videoGrant)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(java.util.Date.from(now))
                .withExpiresAt(java.util.Date.from(exp))
                .sign(algorithm);
    }

    private String extractJti(String token) {
        try {
            String actual = token;
            if (actual.startsWith("Bearer ")) {
                actual = actual.substring(7);
            }
            return com.auth0.jwt.JWT.decode(actual).getId();
        } catch (Exception e) {
            return null;
        }
    }
}
