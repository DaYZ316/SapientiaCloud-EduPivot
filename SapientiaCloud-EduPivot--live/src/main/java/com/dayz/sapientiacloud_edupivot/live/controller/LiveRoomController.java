package com.dayz.sapientiacloud_edupivot.live.controller;

import com.dayz.sapientiacloud_edupivot.live.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.live.common.entity.po.LiveRoom;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.live.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.live.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.live.constant.LiveRoomConstants;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomCreateDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomMessageDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomTokenRequestDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.po.LiveRoomMessage;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomMessageService;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "直播房间管理", description = "直播房间与令牌相关API")
@RestController
@RequestMapping("/live-room")
@RequiredArgsConstructor
public class LiveRoomController extends BaseController {

    private final ILiveRoomService liveRoomService;
    private final ILiveRoomMessageService liveRoomMessageService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${live.sse.token.ttl.seconds:60}")
    private long sseTokenTtlSeconds;

    @HasPermission(summary = "addLiveRoom", description = "创建直播房间并返回房间信息", permission = "LIVE_ROOM_CREATE")
    @PostMapping("/add")
    public Result<LiveRoom> createRoom(@Valid @RequestBody LiveRoomCreateDTO dto) {
        UUID creatorId = UserContextUtil.getCurrentUserId();
        LiveRoom room = liveRoomService.createRoom(dto.getRoomName(), creatorId, dto.getCourseId(), dto.getClassroomId(), dto.getMaxParticipants(), dto.getRecordingEnabled());
        return Result.success(room);
    }

    @HasPermission(summary = "closeLiveRoom", description = "根据房间ID关闭直播房间", permission = "LIVE_ROOM_CLOSE")
    @PostMapping("/close/{id}")
    public Result<Boolean> closeRoom(@PathVariable("id") UUID id) {
        liveRoomService.closeRoom(id);
        return Result.success(true);
    }

    @Operation(summary = "issueRoomToken", description = "根据房间ID与用户角色签发访问令牌")
    @PostMapping("/token/{id}")
    public Result<Map<String, Object>> issueToken(@PathVariable("id") UUID id, @Valid @RequestBody LiveRoomTokenRequestDTO dto) {
        UUID userId = UserContextUtil.getCurrentUserId();
        String username = UserContextUtil.getCurrentUsername();
        String token = liveRoomService.issueToken(id, userId, username, dto.getRole());
        // 返回 token 与后端生成的 LiveKit 房间名（lkRoomName），方便前端使用或校验
        LiveRoom room = liveRoomService.getLiveRoomById(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("roomName", room != null ? room.getLkRoomName() : null);
        return Result.success(resp);
    }

    @Operation(summary = "getActiveLiveRoom", description = "查询当前课程/教室的进行中房间（若存在）")
    @GetMapping("/active")
    public Result<LiveRoom> getActive(@RequestParam(value = "courseId", required = false) UUID courseId,
                                      @RequestParam(value = "classroomId", required = false) UUID classroomId) {
        java.util.List<LiveRoom> list = liveRoomService.listRooms(LiveRoomConstants.STATUS_LIVING, courseId, classroomId);
        if (list != null && !list.isEmpty()) {
            return Result.success(list.get(0));
        }
        return Result.success(null);
    }

    @Operation(summary = "getLatestLiveRoom", description = "查询当前课程/教室的最新房间（不按状态过滤，返回最近一条）")
    @GetMapping("/latest")
    public Result<LiveRoom> getLatest(@RequestParam(value = "courseId", required = false) UUID courseId,
                                      @RequestParam(value = "classroomId", required = false) UUID classroomId) {
        java.util.List<LiveRoom> list = liveRoomService.listRooms(null, courseId, classroomId);
        if (list != null && !list.isEmpty()) {
            return Result.success(list.get(0));
        }
        return Result.success(null);
    }

    @Operation(summary = "listLiveRooms", description = "分页查询直播房间列表")
    @GetMapping("/list")
    public TableDataResult list(@RequestParam(value = "status", required = false) Integer status,
                                @RequestParam(value = "courseId", required = false) UUID courseId,
                                @RequestParam(value = "classroomId", required = false) UUID classroomId) {
        startPage();
        var list = liveRoomService.listRooms(status, courseId, classroomId);
        return getDataTable(list);
    }

    @Operation(summary = "issueSseToken", description = "签发短期 SSE token 用于直播事件订阅")
    @PostMapping("/sse-token")
    public Result<String> issueSseToken(@RequestParam(value = "classroomId", required = false) UUID classroomId) {
        // SSE token 不需要用户登录，可以匿名访问
        String token = UUID.randomUUID().toString();
        Map<String, Object> info = new HashMap<>();
        // 如果有用户登录，则记录用户ID；否则为空（匿名用户）
        try {
            UUID userId = UserContextUtil.getCurrentUserId();
            if (userId != null) {
                info.put("userId", userId.toString());
            }
        } catch (Exception e) {
            // 用户未登录，忽略异常
        }
        info.put("classroomId", classroomId != null ? classroomId.toString() : null);
        redisTemplate.opsForValue().set("sse:token:" + token, info, Duration.ofSeconds(sseTokenTtlSeconds));
        return Result.success(token);
    }

    @Operation(summary = "getLiveRoomDetail", description = "获取直播房间详情")
    @GetMapping("/{id}")
    public Result<LiveRoom> detail(@PathVariable("id") UUID id) {
        LiveRoom room = liveRoomService.getLiveRoomById(id);
        return Result.success(room);
    }

    @HasPermission(summary = "startLiveRoom", description = "根据房间ID开始直播", permission = "LIVE_ROOM_START")
    @PostMapping("/start/{id}")
    public Result<LiveRoom> startLive(@PathVariable("id") UUID id) {
        LiveRoom room = liveRoomService.startLive(id);
        return Result.success(room);
    }

    @HasPermission(summary = "endLiveRoom", description = "根据房间ID结束直播", permission = "LIVE_ROOM_END")
    @PostMapping("/end/{id}")
    public Result<LiveRoom> endLive(@PathVariable("id") UUID id) {
        LiveRoom room = liveRoomService.endLive(id);
        return Result.success(room);
    }


    @Operation(summary = "listLiveRoomMessages", description = "获取直播房间最近的聊天消息")
    @GetMapping("/{id}/messages")
    public Result<List<LiveRoomMessage>> listMessages(@PathVariable("id") UUID id,
                                                      @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit) {
        List<LiveRoomMessage> messages = liveRoomMessageService.listLatestMessages(id, limit != null ? limit : 50);
        return Result.success(messages);
    }

    @Operation(summary = "appendLiveRoomMessage", description = "追加一条直播房间聊天消息")
    @PostMapping("/{id}/message")
    public Result<LiveRoomMessage> appendMessage(@PathVariable("id") UUID id,
                                                 @Valid @RequestBody LiveRoomMessageDTO dto) {
        UUID userId = UserContextUtil.getCurrentUserId();
        String username = UserContextUtil.getCurrentUsername();
        LiveRoomMessage message = liveRoomMessageService.appendMessage(
                id,
                userId,
                username,
                dto.getSenderRole(),
                dto.getContent(),
                dto.getMessageType()
        );
        return Result.success(message);
    }

    @HasPermission(summary = "startRecording", description = "手动开启直播录制", permission = "LIVE_ROOM_RECORD")
    @PostMapping("/{id}/record/start")
    public Result<LiveRoom> startRecording(@PathVariable("id") UUID id) {
        LiveRoom room = liveRoomService.startRecording(id);
        return Result.success(room);
    }

    @HasPermission(summary = "stopRecording", description = "手动停止直播录制", permission = "LIVE_ROOM_RECORD")
    @PostMapping("/{id}/record/stop")
    public Result<LiveRoom> stopRecording(@PathVariable("id") UUID id) {
        LiveRoom room = liveRoomService.stopRecording(id);
        return Result.success(room);
    }
}
