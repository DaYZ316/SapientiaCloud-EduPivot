package com.dayz.sapientiacloud_edupivot.live.service.impl;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dayz.sapientiacloud_edupivot.live.common.clients.CourseRecordClient;
import com.dayz.sapientiacloud_edupivot.live.common.clients.MinIOFileClient;
import com.dayz.sapientiacloud_edupivot.live.common.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.live.common.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.live.common.config.LiveKitProperties;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoomUser;
import com.dayz.sapientiacloud_edupivot.live.common.entity.vo.CourseRecordVO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveRoomServiceImpl implements ILiveRoomService {

    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final long LIVE_START_ADVANCE_MINUTES = 10L;
    private static final long LIVE_END_GRACE_MINUTES = 10L;

    private final LiveRoomMapper liveRoomMapper;
    private final LiveRoomUserMapper liveRoomUserMapper;
    private final LiveKitProperties liveKitProperties;
    private final CourseRecordClient courseRecordClient;
    private final MinIOFileClient minIOFileClient;
    private final TeacherClient teacherClient;
    private final StudentClient studentClient;
    private final LiveKitEgressClient liveKitEgressClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.live-events:live-events-topic}")
    private String liveEventsTopic;

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

        // 如果已有正在进行的直播（LIVE），不要回退状态；仅更新可编辑字段并返回
        if (room.getStatus() != null && room.getStatus().equals(LiveRoomConstants.STATUS_LIVING)) {
            if (StringUtils.hasText(roomName)) {
                room.setRoomName(roomName);
            }
            if (courseId != null) {
                room.setCourseId(courseId);
            }
            room.setMaxParticipants(resolveMaxParticipants(maxParticipants, room.getMaxParticipants()));
            room.setRecordingEnabled(resolveRecordingEnabled(recordingEnabled, room.getRecordingEnabled()));
            liveRoomMapper.updateById(room);
            return room;
        }

        // 普通的初始化/更新逻辑（非 LIVE）
        if (StringUtils.hasText(roomName)) {
            room.setRoomName(roomName);
        }
        if (courseId != null) {
            room.setCourseId(courseId);
        }
        if (creatorId != null && room.getTeacherId() == null) {
            room.setTeacherId(creatorId);
        }
        // 仅在非 LIVE 情况下设置默认状态，避免覆盖 LIVE
        room.setStatus(Objects.requireNonNullElse(room.getStatus(), LiveRoomConstants.STATUS_NOT_STARTED));
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

        // 如果有正在运行的 egress/录制，尝试停止并清理字段
        try {
            if (StringUtils.hasText(room.getEgressTaskId())) {
                LiveKitEgressStopRequest stopRequest = new LiveKitEgressStopRequest();
                stopRequest.setEgressId(room.getEgressTaskId());
                liveKitEgressClient.stopEgress(stopRequest);
                room.setEgressStatus(LiveEgressStatusEnum.STOPPED.getCode());
                room.setEgressTaskId(null);
            }
        } catch (Exception e) {
            // 记录警告但不阻止关闭（可根据业务选择是否回滚）
            // log.warn("stop egress failed", e);
        }

        room.setStatus(LiveRoomConstants.STATUS_ENDED);
        room.setEndTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);

        // publish to kafka for hub forwarding
        try {
            var payload = new HashMap<String, Object>();
            payload.put("event", "close");
            payload.put("roomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("classroomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("courseId", room.getCourseId() != null ? room.getCourseId().toString() : null);
            payload.put("status", room.getStatus());
            payload.put("endTime", room.getEndTime() != null ? room.getEndTime().toString() : null);
            kafkaTemplate.send(liveEventsTopic, room.getId() != null ? room.getId().toString() : null, JSON.toJSONString(payload));
        } catch (Exception ignored) {
        }
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

        if (Objects.equals(room.getStatus(), LiveRoomConstants.STATUS_LIVING) && shouldAutoEnd(room.getId())) {
            endLive(room.getId());
            throw new BusinessException(LiveRoomEnum.LIVE_WINDOW_EXPIRED);
        }

        // ====== 如果是首次进入直播，切换为 LIVE ======
        if (room.getStatus() == LiveRoomConstants.STATUS_NOT_STARTED) {
            ensureLiveCanStart(room);
            room.setStatus(LiveRoomConstants.STATUS_LIVING);
            room.setStartTime(LocalDateTime.now());
            liveRoomMapper.updateById(room);
            autoStartRecordingIfEnabled(room);

            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("event", "start");
                payload.put("roomId", room.getId().toString());
                payload.put("classroomId", room.getId().toString());
                payload.put("courseId", room.getCourseId() != null ? room.getCourseId().toString() : null);
                payload.put("status", room.getStatus());
                payload.put("startTime", room.getStartTime().toString());
                kafkaTemplate.send(liveEventsTopic, room.getId().toString(), JSON.toJSONString(payload));
            } catch (Exception ignored) {
            }
        }

        // ====== ★ 关键修复：确保 lkRoomName 一定存在 ======
        String lkRoomName = room.getLkRoomName();
        if (!StringUtils.hasText(lkRoomName)) {
            String baseName = StringUtils.hasText(room.getRoomName())
                    ? room.getRoomName()
                    : "live";
            lkRoomName = buildLkRoomName(baseName, room.getId());
            room.setLkRoomName(lkRoomName);
            liveRoomMapper.updateById(room);
        }

        // ====== identity & 权限 ======
        String identity = userId + "-" + UUID.randomUUID();

        int resolvedRole = Objects.requireNonNullElse(role, 0);
        boolean canPublish = (resolvedRole == 0 || resolvedRole == 1 || resolvedRole == 2);

        // ====== 生成 LiveKit Token ======
        String token = buildLiveKitAccessToken(
                lkRoomName,
                identity,
                username,
                canPublish
        );

        // ====== 记录 LiveRoomUser ======
        LiveRoomUser liveRoomUser = new LiveRoomUser();
        liveRoomUser.setId(UUID.randomUUID());
        liveRoomUser.setLiveRoomId(roomId);
        liveRoomUser.setCourseId(room.getCourseId());
        liveRoomUser.setRole(resolvedRole);
        liveRoomUser.setJoinTime(LocalDateTime.now());
        liveRoomUser.setLkIdentity(identity);
        liveRoomUser.setTokenJti(extractJti(token));

        boolean isTeacherRole = resolvedRole != 0;
        UUID teacherId = null;
        UUID studentId = null;

        if (isTeacherRole) {
            TeacherVO teacher = fetchTeacherByUserId(userId);
            teacherId = teacher.getId();
            liveRoomUser.setTeacherId(teacherId);
        } else {
            StudentVO student = fetchStudentByUserId(userId);
            studentId = student.getId();
            liveRoomUser.setStudentId(studentId);
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
    public List<LiveRoom> listRooms(Integer status, UUID courseId, UUID classroomId) {
        LambdaQueryWrapper<LiveRoom> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(LiveRoom::getStatus, status);
        }
        if (courseId != null) {
            wrapper.eq(LiveRoom::getCourseId, courseId);
        }
        // 支持按 classroomId（即 course record id）过滤
        if (classroomId != null) {
            wrapper.eq(LiveRoom::getId, classroomId);
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom startLive(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        if (Objects.equals(room.getStatus(), LiveRoomConstants.STATUS_LIVING)) {
            return room;
        }
        ensureLiveCanStart(room);
        room.setStatus(LiveRoomConstants.STATUS_LIVING);
        room.setStartTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);
        autoStartRecordingIfEnabled(room);
        // publish to kafka for hub forwarding
        try {
            var payload = new HashMap<String, Object>();
            payload.put("event", "start");
            payload.put("roomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("classroomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("courseId", room.getCourseId() != null ? room.getCourseId().toString() : null);
            payload.put("status", room.getStatus());
            payload.put("startTime", room.getStartTime() != null ? room.getStartTime().toString() : null);
            kafkaTemplate.send(liveEventsTopic, room.getId() != null ? room.getId().toString() : null, JSON.toJSONString(payload));
        } catch (Exception ignored) {
        }
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom endLive(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        // stop any ongoing egress first
        try {
            if (StringUtils.hasText(room.getEgressTaskId())) {
                LiveKitEgressStopRequest stopRequest = new LiveKitEgressStopRequest();
                stopRequest.setEgressId(room.getEgressTaskId());
                liveKitEgressClient.stopEgress(stopRequest);
                room.setEgressStatus(LiveEgressStatusEnum.STOPPED.getCode());
                room.setEgressTaskId(null);
            }
        } catch (Exception e) {
            // ignore egress stop errors
        }
        room.setStatus(LiveRoomConstants.STATUS_ENDED);
        room.setEndTime(LocalDateTime.now());
        liveRoomMapper.updateById(room);
        // publish to kafka for hub forwarding
        try {
            var payload = new HashMap<String, Object>();
            payload.put("event", "end");
            payload.put("roomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("classroomId", room.getId() != null ? room.getId().toString() : null);
            payload.put("courseId", room.getCourseId() != null ? room.getCourseId().toString() : null);
            payload.put("status", room.getStatus());
            payload.put("endTime", room.getEndTime() != null ? room.getEndTime().toString() : null);
            kafkaTemplate.send(liveEventsTopic, room.getId() != null ? room.getId().toString() : null, JSON.toJSONString(payload));
        } catch (Exception ignored) {
        }
        return room;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LiveRoom discardRecording(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        if (!StringUtils.hasText(room.getRecordingAssetUrl())) {
            return room;
        }
        Result<Boolean> result = minIOFileClient.deleteFileByPath(room.getRecordingAssetUrl());
        if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
            throw new BusinessException(LiveRoomEnum.RECORDING_DELETE_FAILED);
        }
        room.setRecordingAssetUrl(null);
        room.setEgressTaskId(null);
        room.setEgressStatus(LiveEgressStatusEnum.STOPPED.getCode());
        liveRoomMapper.updateById(room);
        return room;
    }

    @Override
    public boolean shouldAutoEnd(UUID roomId) {
        LiveRoom room = requireRoom(roomId);
        return LocalDateTime.now().isAfter(resolveLatestEndTime(room));
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
        return Objects.requireNonNullElse(existing, LiveRoomConstants.RECORDING_ENABLED);
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
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }

    private String extractJti(String token) {
        try {
            String actual = token;
            if (actual.startsWith("Bearer ")) {
                actual = actual.substring(7);
            }
            return JWT.decode(actual).getId();
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

    private CourseRecordVO requireCourseRecord(UUID courseRecordId) {
        Result<CourseRecordVO> result;
        try {
            result = courseRecordClient.getCourseRecordById(courseRecordId);
        } catch (Exception e) {
            log.warn("Fetch course record failed, courseRecordId={}", courseRecordId, e);
            throw new BusinessException(LiveRoomEnum.COURSE_RECORD_SERVICE_ERROR);
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException(LiveRoomEnum.COURSE_RECORD_SERVICE_ERROR);
        }
        CourseRecordVO courseRecord = result.getData();
        if (courseRecord.getStartTime() == null || courseRecord.getOverTime() == null) {
            throw new BusinessException(LiveRoomEnum.COURSE_RECORD_SERVICE_ERROR);
        }
        return courseRecord;
    }

    private void ensureLiveCanStart(LiveRoom room) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (now.isBefore(resolveEarliestStartTime(room))) {
            throw new BusinessException(LiveRoomEnum.LIVE_START_TOO_EARLY);
        }
        if (now.isAfter(resolveLatestEndTime(room))) {
            throw new BusinessException(LiveRoomEnum.LIVE_WINDOW_EXPIRED);
        }
    }

    private LocalDateTime resolveEarliestStartTime(LiveRoom room) {
        return requireCourseRecord(room.getId()).getStartTime().minusMinutes(LIVE_START_ADVANCE_MINUTES);
    }

    private LocalDateTime resolveLatestEndTime(LiveRoom room) {
        return requireCourseRecord(room.getId()).getOverTime().plusMinutes(LIVE_END_GRACE_MINUTES);
    }

    private void ensureRecordingCapability(LiveRoom room) {
        if (!Objects.equals(room.getRecordingEnabled(), LiveRoomConstants.RECORDING_ENABLED)) {
            throw new BusinessException(LiveRoomEnum.RECORDING_NOT_ENABLED);
        }
        if (!Objects.equals(room.getStatus(), LiveRoomConstants.STATUS_LIVING)) {
            throw new BusinessException(LiveRoomEnum.ROOM_NOT_LIVE);
        }
    }

    private void autoStartRecordingIfEnabled(LiveRoom room) {
        if (!Objects.equals(room.getRecordingEnabled(), LiveRoomConstants.RECORDING_ENABLED)) {
            return;
        }
        if (StringUtils.hasText(room.getEgressTaskId())) {
            return;
        }
        if (!isEgressEnabled()) {
            room.setEgressStatus(LiveEgressStatusEnum.FAILED.getCode());
            liveRoomMapper.updateById(room);
            log.warn("Auto recording skipped because egress is disabled, roomId={}", room.getId());
            return;
        }
        String previousRecordingAssetUrl = room.getRecordingAssetUrl();
        try {
            startRecording(room.getId());
        } catch (Exception e) {
            room.setEgressTaskId(null);
            room.setEgressStatus(LiveEgressStatusEnum.FAILED.getCode());
            room.setRecordingAssetUrl(previousRecordingAssetUrl);
            liveRoomMapper.updateById(room);
            log.warn("Auto recording failed, live room remains started, roomId={}", room.getId(), e);
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
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .sign(algorithm);
    }
}
