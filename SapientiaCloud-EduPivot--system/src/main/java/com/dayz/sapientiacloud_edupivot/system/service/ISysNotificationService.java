package com.dayz.sapientiacloud_edupivot.system.service;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationBatchAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationScopeSendDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysNotificationService {

    PageInfo<NotificationVO> listNotificationPage(NotificationQueryDTO queryDTO);

    List<NotificationVO> listAllNotificationByUserId(UUID userId);

    Long getUnreadCount(UUID userId);

    NotificationVO getNotificationById(UUID id);

    NotificationVO addNotification(NotificationAddDTO addDTO);

    Integer batchAddNotification(NotificationBatchAddDTO batchAddDTO);

    Integer sendNotificationByScope(NotificationScopeSendDTO scopeSendDTO);

    Boolean updateNotification(NotificationDTO notificationDTO);

    Boolean markAsRead(UUID id);

    Integer batchMarkAsRead(List<UUID> ids);

    Integer markAllAsRead(UUID userId);

    Boolean removeNotificationMsg(UUID msgId);

    Boolean removeNotificationById(UUID id);

    Integer removeNotificationByIds(List<UUID> ids);
}

