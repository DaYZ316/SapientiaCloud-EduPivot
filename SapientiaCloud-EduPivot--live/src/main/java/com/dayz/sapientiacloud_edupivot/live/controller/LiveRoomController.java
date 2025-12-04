package com.dayz.sapientiacloud_edupivot.live.controller;

import com.dayz.sapientiacloud_edupivot.live.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomCreateDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomMessageDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.dto.LiveRoomTokenRequestDTO;
import com.dayz.sapientiacloud_edupivot.live.entity.po.LiveRoomMessage;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomMessageService;
import com.dayz.sapientiacloud_edupivot.live.service.ILiveRoomService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.dayz.sapientiacloud_edupivot.live.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.live.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.live.common.result.TableDataResult;

@Tag(name = "直播房间管理", description = "直播房间与令牌相关API")
@RestController
@RequestMapping("/live-room")
@RequiredArgsConstructor
public class LiveRoomController extends BaseController {

    private final ILiveRoomService liveRoomService;
    private final ILiveRoomMessageService liveRoomMessageService;

    @HasPermission(summary = "addLiveRoom", description = "创建直播房间并返回房间信息", permission = "LIVE_ROOM_CREATE")
    @PostMapping("/add")
    public Result<?> createRoom(@Valid @RequestBody LiveRoomCreateDTO dto) {
        UUID creatorId = UserContextUtil.getCurrentUserId();
        var room = liveRoomService.createRoom(dto.getRoomName(), creatorId, dto.getCourseId(), dto.getClassroomId(), dto.getMaxParticipants(), dto.getRecordingEnabled());
        return Result.success(room);
    }

    @HasPermission(summary = "closeLiveRoom", description = "根据房间ID关闭直播房间", permission = "LIVE_ROOM_CLOSE")
    @PostMapping("/close/{id}")
    public Result<?> closeRoom(@PathVariable("id") UUID id) {
        liveRoomService.closeRoom(id);
        return Result.success();
    }

    @Operation(summary = "issueRoomToken", description = "根据房间ID与用户角色签发访问令牌")
    @PostMapping("/token/{id}")
    public Result<?> issueToken(@PathVariable("id") UUID id, @Valid @RequestBody LiveRoomTokenRequestDTO dto) {
        UUID userId = UserContextUtil.getCurrentUserId();
        String username = UserContextUtil.getCurrentUsername();
        String token = liveRoomService.issueToken(id, userId, username, dto.getRole());
        return Result.success(token);
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

    @Operation(summary = "getLiveRoomDetail", description = "获取直播房间详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable("id") UUID id) {
        var room = liveRoomService.getById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        return Result.success(room);
    }

    @Operation(summary = "listLiveRoomMessages", description = "获取直播房间最近的聊天消息")
    @GetMapping("/{id}/messages")
    public Result<?> listMessages(@PathVariable("id") UUID id,
                                  @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit) {
        var messages = liveRoomMessageService.listLatestMessages(id, limit != null ? limit : 50);
        return Result.success(messages);
    }

    @Operation(summary = "appendLiveRoomMessage", description = "追加一条直播房间聊天消息")
    @PostMapping("/{id}/message")
    public Result<?> appendMessage(@PathVariable("id") UUID id,
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
    public Result<?> startRecording(@PathVariable("id") UUID id) {
        var room = liveRoomService.startRecording(id);
        return Result.success(room);
    }

    @HasPermission(summary = "stopRecording", description = "手动停止直播录制", permission = "LIVE_ROOM_RECORD")
    @PostMapping("/{id}/record/stop")
    public Result<?> stopRecording(@PathVariable("id") UUID id) {
        var room = liveRoomService.stopRecording(id);
        return Result.success(room);
    }
}
