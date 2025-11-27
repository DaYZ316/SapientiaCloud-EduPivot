package com.dayz.sapientiacloud_edupivot.live.room.controller;

import com.dayz.sapientiacloud_edupivot.live.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.live.room.service.ILiveRoomService;
import com.dayz.sapientiacloud_edupivot.live.room.dto.LiveRoomCreateDTO;
import com.dayz.sapientiacloud_edupivot.live.room.dto.LiveRoomTokenRequestDTO;
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
public class LiveRoomController extends BaseController {

    private final ILiveRoomService liveRoomService;

    public LiveRoomController(ILiveRoomService liveRoomService) {
        this.liveRoomService = liveRoomService;
    }

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
}
