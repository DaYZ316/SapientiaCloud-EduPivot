package com.dayz.sapientiacloud_edupivot.live.service;

import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;

import java.util.UUID;

public interface ILiveRoomService {
    LiveRoom createRoom(String roomName, UUID creatorId, UUID courseId, UUID classroomId, Integer maxParticipants, Integer recordingEnabled);

    void closeRoom(UUID roomId);

    String issueToken(UUID roomId, UUID userId, String username, Integer role);

    LiveRoom getLiveRoomById(UUID id);

    java.util.List<LiveRoom> listRooms(Integer status, UUID courseId, UUID classroomId);

    LiveRoom startRecording(UUID roomId);

    LiveRoom stopRecording(UUID roomId);

    LiveRoom startLive(UUID roomId);

    LiveRoom endLive(UUID roomId);
}
