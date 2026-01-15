package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.system.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysNotificationService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统通知管理", description = "提供系统通知的发送、查询、状态管理及撤回功能")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class SysNotificationController extends BaseController {

    private final ISysNotificationService notificationService;

    @HasPermission(
            summary = "listNotification",
            description = "分页查询用户的通知列表（包含已读/未读状态、消息内容）",
            permission = PermissionConstants.NOTIFICATION_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listNotification(@ParameterObject NotificationQueryDTO queryDTO) {
        startPage();
        PageInfo<NotificationVO> pageInfo = notificationService.listNotificationPage(queryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllNotification",
            description = "获取当前用户的所有通知列表（不分页，按时间倒序）",
            permission = PermissionConstants.NOTIFICATION_QUERY
    )
    @GetMapping("/all")
    public Result<List<NotificationVO>> listAllNotification() {
        UUID userId = UserContextUtil.getCurrentUserId();
        List<NotificationVO> list = notificationService.listAllNotificationByUserId(userId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "getUnreadCount",
            description = "获取当前用户的未读通知总数",
            permission = PermissionConstants.NOTIFICATION_QUERY
    )
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        UUID userId = UserContextUtil.getCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @HasPermission(
            summary = "getNotificationById",
            description = "根据收件箱ID获取通知详情（包含完整内容）",
            permission = PermissionConstants.NOTIFICATION_QUERY
    )
    @GetMapping("/{id}")
    public Result<NotificationVO> getNotificationById(
            @Parameter(name = "id", description = "用户收件箱记录ID", required = true)
            @PathVariable("id") UUID id
    ) {
        NotificationVO vo = notificationService.getNotificationById(id);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "addNotification",
            description = "发送单条通知给指定用户",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/add")
    public Result<NotificationVO> addNotification(@Valid @RequestBody NotificationAddDTO addDTO) {
        NotificationVO vo = notificationService.addNotification(addDTO);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "batchAddNotification",
            description = "批量发送通知给多个用户",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/batch-add")
    public Result<Integer> batchAddNotification(@Valid @RequestBody NotificationBatchAddDTO batchAddDTO) {
        Integer count = notificationService.batchAddNotification(batchAddDTO);
        return Result.success(count);
    }

    @HasPermission(
            summary = "sendNotificationByScope",
            description = "按范围群发通知（支持按角色Key或课程ID群发）",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/send-by-scope")
    public Result<Integer> sendNotificationByScope(@Valid @RequestBody NotificationScopeSendDTO scopeSendDTO) {
        Integer count = notificationService.sendNotificationByScope(scopeSendDTO);
        return Result.success(count);
    }

    @HasPermission(
            summary = "updateNotification",
            description = "修改通知消息内容（仅限管理员修改消息体，不影响用户阅读状态）",
            permission = PermissionConstants.NOTIFICATION_EDIT
    )
    @PutMapping
    public Result<Boolean> updateNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        return Result.success(notificationService.updateNotification(notificationDTO));
    }

    @HasPermission(
            summary = "markAsRead",
            description = "标记单条通知为已读"
    )
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(
            @Parameter(name = "id", description = "用户收件箱记录ID", required = true)
            @PathVariable("id") UUID id
    ) {
        return Result.success(notificationService.markAsRead(id));
    }

    @HasPermission(
            summary = "batchMarkAsRead",
            description = "批量标记多条通知为已读"
    )
    @PutMapping("/batch-read")
    public Result<Integer> batchMarkAsRead(
            @Parameter(name = "ids", description = "用户收件箱记录ID列表", required = true)
            @RequestBody List<UUID> ids
    ) {
        return Result.success(notificationService.batchMarkAsRead(ids));
    }

    @HasPermission(
            summary = "markAllAsRead",
            description = "一键标记当前用户所有通知为已读"
    )
    @PutMapping("/read-all")
    public Result<Integer> markAllAsRead() {
        UUID userId = UserContextUtil.getCurrentUserId();
        return Result.success(notificationService.markAllAsRead(userId));
    }

    @HasPermission(
            summary = "removeNotificationMsg",
            description = "管理员撤回/逻辑删除通知消息（所有接收者将不可见）",
            permission = PermissionConstants.NOTIFICATION_DELETE
    )
    @DeleteMapping("/msg/{id}")
    public Result<Boolean> removeNotificationMsg(
            @Parameter(name = "id", description = "通知消息ID (MsgID)", required = true)
            @PathVariable("id") UUID id
    ) {
        Boolean removed = notificationService.removeNotificationMsg(id);
        if (Boolean.TRUE.equals(removed)) {
            return Result.success(true);
        }
        throw new BusinessException(ResultEnum.FAIL);
    }

    @HasPermission(
            summary = "removeNotificationById",
            description = "用户删除单条通知（仅从自己的收件箱移除）",
            permission = PermissionConstants.NOTIFICATION_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeNotificationById(
            @Parameter(name = "id", description = "用户收件箱记录ID", required = true)
            @PathVariable("id") UUID id
    ) {
        return Result.success(notificationService.removeNotificationById(id));
    }

    @HasPermission(
            summary = "removeNotificationByIds",
            description = "用户批量删除通知（仅从自己的收件箱移除）",
            permission = PermissionConstants.NOTIFICATION_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeNotificationByIds(
            @Parameter(name = "ids", description = "用户收件箱记录ID列表", required = true)
            @RequestBody List<UUID> ids
    ) {
        return Result.success(notificationService.removeNotificationByIds(ids));
    }
}

