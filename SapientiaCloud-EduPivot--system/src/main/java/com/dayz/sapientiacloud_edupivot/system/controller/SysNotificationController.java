package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationBatchAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationScopeSendDTO;
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

@Tag(name = "通知管理", description = "用于管理系统通知的API")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class SysNotificationController extends BaseController {

    private final ISysNotificationService notificationService;

    @HasPermission(
            summary = "listNotification",
            description = "分页查询通知列表",
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
            description = "获取当前用户所有通知列表",
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
            description = "获取当前用户未读通知数量",
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
            description = "根据ID获取通知详情",
            permission = PermissionConstants.NOTIFICATION_QUERY
    )
    @GetMapping("/{id}")
    public Result<NotificationVO> getNotificationById(
            @Parameter(name = "id", description = "通知ID", required = true)
            @PathVariable("id") UUID id
    ) {
        NotificationVO vo = notificationService.getNotificationById(id);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "addNotification",
            description = "创建通知",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/add")
    public Result<NotificationVO> addNotification(@Valid @RequestBody NotificationAddDTO addDTO) {
        NotificationVO vo = notificationService.addNotification(addDTO);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "batchAddNotification",
            description = "批量创建通知",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/batch-add")
    public Result<Integer> batchAddNotification(@Valid @RequestBody NotificationBatchAddDTO batchAddDTO) {
        NotificationAddDTO addDTO = new NotificationAddDTO();
        addDTO.setTitle(batchAddDTO.getTitle());
        addDTO.setContent(batchAddDTO.getContent());
        addDTO.setType(batchAddDTO.getType());
        addDTO.setSenderId(batchAddDTO.getSenderId());
        addDTO.setSenderName(batchAddDTO.getSenderName());

        Integer count = notificationService.batchAddNotification(batchAddDTO.getUserIds(), addDTO);
        return Result.success(count);
    }

    @HasPermission(
            summary = "sendNotificationByScope",
            description = "按范围（角色或课程）发送通知",
            permission = PermissionConstants.NOTIFICATION_ADD
    )
    @PostMapping("/send-by-scope")
    public Result<Integer> sendNotificationByScope(@Valid @RequestBody NotificationScopeSendDTO scopeSendDTO) {
        Integer count = notificationService.sendNotificationByScope(scopeSendDTO);
        return Result.success(count);
    }

    @HasPermission(
            summary = "updateNotification",
            description = "更新通知",
            permission = PermissionConstants.NOTIFICATION_EDIT
    )
    @PutMapping
    public Result<Boolean> updateNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        return Result.success(notificationService.updateNotification(notificationDTO));
    }

    @HasPermission(
            summary = "markAsRead",
            description = "标记通知为已读"
    )
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(
            @Parameter(name = "id", description = "通知ID", required = true)
            @PathVariable("id") UUID id
    ) {
        return Result.success(notificationService.markAsRead(id));
    }

    @HasPermission(
            summary = "batchMarkAsRead",
            description = "批量标记通知为已读"
    )
    @PutMapping("/batch-read")
    public Result<Integer> batchMarkAsRead(
            @Parameter(name = "ids", description = "通知ID列表", required = true)
            @RequestBody List<UUID> ids
    ) {
        return Result.success(notificationService.batchMarkAsRead(ids));
    }

    @HasPermission(
            summary = "markAllAsRead",
            description = "标记当前用户所有通知为已读"
    )
    @PutMapping("/read-all")
    public Result<Integer> markAllAsRead() {
        UUID userId = UserContextUtil.getCurrentUserId();
        return Result.success(notificationService.markAllAsRead(userId));
    }

    @HasPermission(
            summary = "removeNotificationById",
            description = "删除通知",
            permission = PermissionConstants.NOTIFICATION_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeNotificationById(
            @Parameter(name = "id", description = "通知ID", required = true)
            @PathVariable("id") UUID id
    ) {
        return Result.success(notificationService.removeNotificationById(id));
    }

    @HasPermission(
            summary = "removeNotificationByIds",
            description = "批量删除通知",
            permission = PermissionConstants.NOTIFICATION_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeNotificationByIds(
            @Parameter(name = "ids", description = "通知ID列表", required = true)
            @RequestBody List<UUID> ids
    ) {
        return Result.success(notificationService.removeNotificationByIds(ids));
    }
}

