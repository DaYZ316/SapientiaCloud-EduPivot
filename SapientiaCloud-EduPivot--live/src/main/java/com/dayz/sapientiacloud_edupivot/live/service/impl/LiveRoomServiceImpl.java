package com.dayz.sapientiacloud_edupivot.live.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dayz.sapientiacloud_edupivot.live.common.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.live.common.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.live.common.config.LiveKitProperties;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoomUser;
import com.dayz.sapientiacloud_edupivot.live.common.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.live.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.LiveKitEgressClient;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressFileOutput;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartRequest;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStartResponse;
import com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto.LiveKitEgressStopRequest;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.constant.LiveRoomConstants;
import com.dayz.sapientiacloud_edupivot.live.enums.LiveEgressStatusEnum;
import com.dayz.sapientiacloud_edupivot.live.enums.LiveRoomEnum;
import com.dayz.sapientiacloud_edupivot.live.mapper.LiveRoomMapper;
import com.dayz.sapientiacloud_edupivot.live.mapper.LiveRoomUserMapper;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements ILiveRoomService {

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final LiveRoomMapper liveRoomMapper;
    private final LiveRoomUserMapper liveRoomUserMapper;
    private final LiveKitProperties liveKitProperties;
    private final TeacherClient teacherClient;
    private final StudentClient studentClient;
    private final LiveKitEgressClient liveKitEgressClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom createRoom(String roomName, UUID creatorId, UUID courseId, UUID classroomId, Integer maxParticipants, Integer recordingEnabled) {
        if (classroomId == null) {
            throw new BusinessException(LiveRoomEnum.CLASSROOM_ID_REQUIRED);
        }
        LiveRoom room = liveRoomMapper.selectById(classroomId);
        if (room == null) {
            throw new BusinessException(LiveRoomEnum.CLASSROOM_NOT_EXISTS);
        }
        room.setRoomName(roomName);
        if (courseId != null) {
            room.setCourseId(courseId);
        }
        if (creatorId != null && room.getTeacherId() == null) {
            room.setTeacherId(creatorId);
        }
        room.setStatus(LiveRoomConstants.STATUS_NOT_STARTED);
        String lkRoomName = room.getLkRoomName();
        if (!StringUtils.hasText(lkRoomName)) {
            lkRoomName = buildLkRoomName(roomName, classroomId);
        }
        room.setLkRoomName(lkRoomName);
        room.setMaxParticipants(resolveMaxParticipants(maxParticipants, room.getMaxParticipants()));
        room.setRecordingEnabled(resolveRecordingEnabled(recordingEnabled, room.getRecordingEnabled()));
        liveRoomMapper.updateById(room);
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeRoom(UUID roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_EXISTS);
        }
        room.setStatus(LiveRoomConstants.STATUS_ENDED);
        room.setEndTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String issueToken(UUID roomId, UUID userId, String username, Integer role) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_EXISTS);
        }
        
        boolean isStudent = role == null || role == 0;
        if (isStudent && room.getStatus() != LiveRoomConstants.STATUS_LIVING) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_LIVE_FOR_STUDENT);
        }
        
        if (room.getStatus() == LiveRoomConstants.STATUS_NOT_STARTED) {
            room.setStatus(LiveRoomConstants.STATUS_LIVING);
            room.setStartTime(LocalDateTime.now());
            liveRoomMapper.updateById(room);
        }
        String identity = userId + "-" + UUID.randomUUID();
        // 允许学生(0)、老师(1)、助教(2)发布音视频
        boolean canPublish = role != null && (role == 0 || role == 1 || role == 2);
        String token = buildLiveKitAccessToken(room.getLkRoomName(), identity, username, canPublish);

        LiveRoomUser liveRoomUser = new LiveRoomUser();
        liveRoomUser.setId(UUID.randomUUID());
        liveRoomUser.setLiveRoomId(roomId);
        liveRoomUser.setCourseId(room.getCourseId());
        liveRoomUser.setRole(Objects.requireNonNullElse(role, 0));
        liveRoomUser.setJoinTime(LocalDateTime.now());
        liveRoomUser.setLkIdentity(identity);
        liveRoomUser.setTokenJti(extractJti(token));

        boolean isTeacherRole = role != null && role != 0;
        UUID teacherId = null;
        UUID studentId = null;
        if (isTeacherRole) {
            TeacherVO teacher = fetchTeacherByUserId(userId);
            teacherId = teacher.getId();
        } else {
            StudentVO student = fetchStudentByUserId(userId);
            studentId = student.getId();
        }

        if (isTeacherRole) {
            liveRoomUser.setTeacherId(teacherId);
            liveRoomUser.setStudentId(null);
        } else {
            liveRoomUser.setStudentId(studentId);
            liveRoomUser.setTeacherId(null);
        }

        LambdaQueryWrapper<LiveRoomUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LiveRoomUser::getLiveRoomId, roomId)
                .eq(isTeacherRole, LiveRoomUser::getTeacherId, teacherId)
                .eq(!isTeacherRole, LiveRoomUser::getStudentId, studentId)
                .isNull(LiveRoomUser::getLeaveTime)
                .last("limit 1");
        LiveRoomUser existing = liveRoomUserMapper.selectOne(wrapper);
        if (existing == null) {
            liveRoomUserMapper.insert(liveRoomUser);
        }

        return token;
    }

    @Override
    public LiveRoom getLiveRoomById(UUID id) {
        if (id == null) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_EXISTS);
        }
        LiveRoom room = liveRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_EXISTS);
        }
        return room;
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
        // 说明：mg_course_record 中目前无 classroomId 字段，这里暂不按 classroomId 过滤
        wrapper.orderByDesc(LiveRoom::getStartTime);
        return liveRoomMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom startRecording(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        ensureRecordingCapability(room);
        if (StringUtils.hasText(room.getEgressTaskId())) {
            throw new BusinessException(LiveRoomEnum.RECORDING_ALREADY_RUNNING);
        }
        if (!isEgressEnabled()) {
            throw new BusinessException(LiveRoomEnum.RECORDING_ENV_DISABLED);
        }
        LiveKitEgressStartRequest request = buildEgressStartRequest(room);
        LiveKitEgressStartResponse response = liveKitEgressClient.startCompositeEgress(request);
        room.setEgressTaskId(response.getEgressId());
        room.setEgressStatus(LiveEgressStatusEnum.RUNNING.getCode());
        liveRoomMapper.updateById(room);
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom stopRecording(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        if (!StringUtils.hasText(room.getEgressTaskId())) {
            throw new BusinessException(LiveRoomEnum.RECORDING_NOT_RUNNING);
        }
        LiveKitEgressStopRequest stopRequest = new LiveKitEgressStopRequest();
        stopRequest.setEgressId(room.getEgressTaskId());
        liveKitEgressClient.stopEgress(stopRequest);
        room.setEgressStatus(LiveEgressStatusEnum.STOPPED.getCode());
        room.setEgressTaskId(null);
        liveRoomMapper.updateById(room);
        return room;
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
        return LiveRoomConstants.DEFAULT_MAX_PARTICIPANTS;
    }

    private int resolveMaxParticipants(Integer requested, Integer existing) {
        if (requested != null) {
            return requested;
        }
        if (existing != null) {
            return existing;
        }
        return defaultMaxParticipants();
    }

    private int resolveRecordingEnabled(Integer requested, Integer existing) {
        if (requested != null) {
            return requested;
        }
        return Objects.requireNonNullElse(existing, LiveRoomConstants.RECORDING_DISABLED);
    }

    private TeacherVO fetchTeacherByUserId(UUID userId) {
        Result<TeacherVO> result = teacherClient.getTeacherByUserId(userId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException(LiveRoomEnum.TEACHER_SERVICE_ERROR);
        }
        return result.getData();
    }

    private StudentVO fetchStudentByUserId(UUID userId) {
        Result<StudentVO> result = studentClient.getStudentByUserId(userId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException(LiveRoomEnum.STUDENT_SERVICE_ERROR);
        }
        return result.getData();
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

    private LiveRoom requireRoom(UUID roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_EXISTS);
        }
        return room;
    }

    private void ensureRecordingCapability(LiveRoom room) {
        if (!Objects.equals(room.getRecordingEnabled(), LiveRoomConstants.RECORDING_ENABLED)) {
            throw new BusinessException(LiveRoomEnum.RECORDING_NOT_ENABLED);
        }
        if (!Objects.equals(room.getStatus(), LiveRoomConstants.STATUS_LIVING)) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_LIVE);
        }
    }

    private boolean isEgressEnabled() {
        return Optional.ofNullable(liveKitProperties.getEgress())
                .map(LiveKitProperties.EgressDefaults::getEnable)
                .orElse(Boolean.FALSE);
    }

    private LiveKitEgressStartRequest buildEgressStartRequest(LiveRoom room) {
        LiveKitProperties.EgressDefaults egress = liveKitProperties.getEgress();
        String layout = egress != null && StringUtils.hasText(egress.getLayout()) ? egress.getLayout() : "speaker-dark";
        String fileType = egress != null && StringUtils.hasText(egress.getFileType()) ? egress.getFileType() : "MP4";
        String filepath = buildOutputFilepath(room);
        LiveKitEgressFileOutput fileOutput = new LiveKitEgressFileOutput();
        fileOutput.setFileType(fileType);
        fileOutput.setFilepath(filepath);

        LiveKitEgressStartRequest request = new LiveKitEgressStartRequest();
        if (!StringUtils.hasText(room.getLkRoomName())) {
            room.setLkRoomName(buildLkRoomName(room.getRoomName(), room.getId()));
        }
        request.setRoomName(room.getLkRoomName());
        request.setLayout(layout);
        // 不等待 start_signal，也不等待轨道
        request.setAwaitStartSignal(false);
        request.setWaitForTrack(false);
        // 录制端访问房间的 token（只订阅 + roomRecord）
        request.setToken(buildEgressAccessToken(room.getLkRoomName()));
        request.setFileOutputs(Collections.singletonList(fileOutput));

        room.setRecordingAssetUrl(buildPlaybackUrl(filepath));

        return request;
    }

    private String buildOutputFilepath(LiveRoom room) {
        LiveKitProperties.EgressDefaults egress = liveKitProperties.getEgress();
        String prefix = egress != null && StringUtils.hasText(egress.getOutputPrefix())
                ? egress.getOutputPrefix()
                : "live-playback";
        LocalDateTime now = LocalDateTime.now();
        String fileName = FILE_NAME_FORMATTER.format(now) + "-" + room.getId() + ".mp4";
        return prefix + "/" + room.getId() + "/" + fileName;
    }

    private String buildPlaybackUrl(String relativePath) {
        LiveKitProperties.EgressDefaults egress = liveKitProperties.getEgress();
        if (egress != null && StringUtils.hasText(egress.getPlaybackBaseUrl())) {
            return egress.getPlaybackBaseUrl().replaceAll("/$", "") + "/" + relativePath;
        }
        return relativePath;
    }


    private String buildEgressAccessToken(String roomName) {
        Algorithm algorithm = Algorithm.HMAC256(liveKitProperties.getApiSecret());
        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("roomJoin", true);
        videoGrant.put("room", roomName);
        videoGrant.put("canSubscribe", true);
        videoGrant.put("canPublish", false);
        videoGrant.put("canPublishData", true);
        videoGrant.put("roomRecord", true);

        Instant now = Instant.now();
        int ttl = Objects.requireNonNullElse(liveKitProperties.getTokenTtlSeconds(), 7200);
        Instant exp = now.plusSeconds(ttl);

        return JWT.create()
                .withIssuer(liveKitProperties.getApiKey())
                .withSubject("egress-recorder")
                .withClaim("name", "egress-recorder")
                .withClaim("video", videoGrant)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(java.util.Date.from(now))
                .withExpiresAt(java.util.Date.from(exp))
                .sign(algorithm);
    }
}
