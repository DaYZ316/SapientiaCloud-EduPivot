package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.system.common.clients.vo.CourseStudentClientVO;
import com.dayz.sapientiacloud_edupivot.system.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationScopeSendDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotification;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationStatusEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationTargetScopeEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationTypeEnum;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysNotificationMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysNotificationService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification> implements ISysNotificationService {

    private final SysNotificationMapper sysNotificationMapper;
    private final CourseClient courseClient;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<NotificationVO> listNotificationPage(NotificationQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new NotificationQueryDTO();
        }

        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<NotificationVO> list = sysNotificationMapper.listNotification(queryDTO);
        PageInfo<NotificationVO> pageInfo = new PageInfo<>(convertToVOList(list));
        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationVO> listAllNotificationByUserId(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        LambdaQueryWrapper<SysNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getDeleted, DeletedEnum.NOT_DELETED.getCode())
                .orderByDesc(SysNotification::getCreateTime);

        List<SysNotification> notificationList = this.list(queryWrapper);
        return convertToVOList(notificationList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        LambdaQueryWrapper<SysNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getStatus, NotificationStatusEnum.UNREAD.getCode())
                .eq(SysNotification::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        return this.count(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationVO getNotificationById(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotification notification = this.getById(id);
        if (notification == null || notification.getDeleted() != null && notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        return convertToVO(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationVO addNotification(NotificationAddDTO addDTO) {
        if (addDTO == null) {
            throw new BusinessException(NotificationEnum.DATA_CANNOT_BE_EMPTY);
        }

        if (addDTO.getUserId() == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        if (!StringUtils.hasText(addDTO.getTitle())) {
            throw new BusinessException(NotificationEnum.TITLE_REQUIRED);
        }

        if (addDTO.getType() != null && !NotificationTypeEnum.isValidCode(addDTO.getType())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_TYPE_INVALID);
        }

        SysNotification notification = new SysNotification();
        notification.setId(UuidCreator.getTimeOrderedEpoch());
        notification.setUserId(addDTO.getUserId());
        notification.setTitle(addDTO.getTitle());
        notification.setContent(addDTO.getContent());
        notification.setAttachmentUrls(addDTO.getAttachmentUrls());
        notification.setType(addDTO.getType() != null ? addDTO.getType() : NotificationTypeEnum.SYSTEM.getCode());
        notification.setStatus(NotificationStatusEnum.UNREAD.getCode());
        notification.setSenderId(addDTO.getSenderId());
        notification.setSenderName(addDTO.getSenderName());
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notification.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        this.save(notification);
        return convertToVO(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAddNotification(List<UUID> userIds, NotificationAddDTO addDTO) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        if (addDTO == null) {
            throw new BusinessException(NotificationEnum.DATA_CANNOT_BE_EMPTY);
        }

        if (!StringUtils.hasText(addDTO.getTitle())) {
            throw new BusinessException(NotificationEnum.TITLE_REQUIRED);
        }

        if (addDTO.getType() != null && !NotificationTypeEnum.isValidCode(addDTO.getType())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_TYPE_INVALID);
        }

        List<SysNotification> notificationList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (UUID userId : userIds) {
            SysNotification notification = new SysNotification();
            notification.setId(UuidCreator.getTimeOrderedEpoch());
            notification.setUserId(userId);
            notification.setTitle(addDTO.getTitle());
            notification.setContent(addDTO.getContent());
            notification.setAttachmentUrls(addDTO.getAttachmentUrls());
            notification.setType(addDTO.getType() != null ? addDTO.getType() : NotificationTypeEnum.SYSTEM.getCode());
            notification.setStatus(NotificationStatusEnum.UNREAD.getCode());
            notification.setSenderId(addDTO.getSenderId());
            notification.setSenderName(addDTO.getSenderName());
            notification.setCreateTime(now);
            notification.setUpdateTime(now);
            notification.setDeleted(DeletedEnum.NOT_DELETED.getCode());

            notificationList.add(notification);
        }

        this.saveBatch(notificationList);
        return notificationList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer sendNotificationByScope(NotificationScopeSendDTO scopeSendDTO) {
        if (scopeSendDTO == null) {
            throw new BusinessException(NotificationEnum.DATA_CANNOT_BE_EMPTY);
        }

        if (!NotificationTargetScopeEnum.isValidCode(scopeSendDTO.getScopeType())) {
            throw new BusinessException(NotificationEnum.TARGET_SCOPE_INVALID);
        }

        // 解析目标用户ID列表
        List<UUID> targetUserIds = new ArrayList<>();

        if (NotificationTargetScopeEnum.ROLE.getCode() == scopeSendDTO.getScopeType()) {
            // 按角色发送
            if (!StringUtils.hasText(scopeSendDTO.getRoleKey())) {
                throw new BusinessException(NotificationEnum.ROLE_KEY_REQUIRED);
            }

            SysRole role = sysRoleMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleKey, scopeSendDTO.getRoleKey())
            );
            if (role == null) {
                throw new BusinessException(NotificationEnum.ROLE_NOT_EXISTS);
            }

            List<UUID> userIds = sysUserRoleMapper.getUserIdsByRoleId(role.getId());
            if (!CollectionUtils.isEmpty(userIds)) {
                targetUserIds.addAll(userIds);
            }
        } else if (NotificationTargetScopeEnum.COURSE_STUDENT.getCode() == scopeSendDTO.getScopeType()) {
            // 按课程学生发送
            if (scopeSendDTO.getCourseId() == null) {
                throw new BusinessException(NotificationEnum.COURSE_ID_REQUIRED);
            }

            var result = courseClient.listStudentsByCourseId(scopeSendDTO.getCourseId());
            if (result != null && result.isSuccess() && !CollectionUtils.isEmpty(result.getData())) {
                for (CourseStudentClientVO vo : result.getData()) {
                    if (vo != null && vo.getSysUserId() != null) {
                        targetUserIds.add(vo.getSysUserId());
                    }
                }
            }
        }

        // 去重
        if (!CollectionUtils.isEmpty(targetUserIds)) {
            targetUserIds = targetUserIds.stream()
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
        }

        if (CollectionUtils.isEmpty(targetUserIds)) {
            throw new BusinessException(NotificationEnum.TARGET_USER_NOT_FOUND);
        }

        NotificationAddDTO addDTO = new NotificationAddDTO();
        addDTO.setTitle(scopeSendDTO.getTitle());
        addDTO.setContent(scopeSendDTO.getContent());
        addDTO.setType(scopeSendDTO.getType());
        addDTO.setSenderId(scopeSendDTO.getSenderId());
        addDTO.setSenderName(scopeSendDTO.getSenderName());
        addDTO.setAttachmentUrls(scopeSendDTO.getAttachmentUrls());

        return batchAddNotification(targetUserIds, addDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateNotification(NotificationDTO notificationDTO) {
        if (notificationDTO == null || notificationDTO.getId() == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotification notification = this.getById(notificationDTO.getId());
        if (notification == null || notification.getDeleted() != null && notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        if (StringUtils.hasText(notificationDTO.getTitle())) {
            notification.setTitle(notificationDTO.getTitle());
        }

        if (notificationDTO.getContent() != null) {
            notification.setContent(notificationDTO.getContent());
        }

        if (notificationDTO.getAttachmentUrls() != null) {
            notification.setAttachmentUrls(notificationDTO.getAttachmentUrls());
        }

        if (notificationDTO.getType() != null) {
            if (!NotificationTypeEnum.isValidCode(notificationDTO.getType())) {
                throw new BusinessException(NotificationEnum.NOTIFICATION_TYPE_INVALID);
            }
            notification.setType(notificationDTO.getType());
        }

        notification.setUpdateTime(LocalDateTime.now());

        return this.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotification notification = this.getById(id);
        if (notification == null || notification.getDeleted() != null && notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        notification.setStatus(NotificationStatusEnum.READ.getCode());
        notification.setReadTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());

        return this.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchMarkAsRead(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_IDS_REQUIRED);
        }

        List<SysNotification> notificationList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(notificationList)) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;

        for (SysNotification notification : notificationList) {
            if (notification.getDeleted() == null || !notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
                notification.setStatus(NotificationStatusEnum.READ.getCode());
                notification.setReadTime(now);
                notification.setUpdateTime(now);
                successCount++;
            }
        }

        if (successCount > 0) {
            this.updateBatchById(notificationList);
        }

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer markAllAsRead(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        LambdaQueryWrapper<SysNotification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotification::getUserId, userId)
                .eq(SysNotification::getStatus, NotificationStatusEnum.UNREAD.getCode())
                .eq(SysNotification::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        List<SysNotification> notificationList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(notificationList)) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        notificationList.forEach(notification -> {
            notification.setStatus(NotificationStatusEnum.READ.getCode());
            notification.setReadTime(now);
            notification.setUpdateTime(now);
        });

        this.updateBatchById(notificationList);
        return notificationList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeNotificationById(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotification notification = this.getById(id);
        if (notification == null || notification.getDeleted() != null && notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        notification.setDeleted(DeletedEnum.DELETED.getCode());
        notification.setUpdateTime(LocalDateTime.now());

        return this.updateById(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer removeNotificationByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_IDS_REQUIRED);
        }

        List<SysNotification> notificationList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(notificationList)) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;

        for (SysNotification notification : notificationList) {
            if (notification.getDeleted() == null || !notification.getDeleted().equals(DeletedEnum.DELETED.getCode())) {
                notification.setDeleted(DeletedEnum.DELETED.getCode());
                notification.setUpdateTime(now);
                successCount++;
            }
        }

        if (successCount > 0) {
            this.updateBatchById(notificationList);
        }

        return successCount;
    }

    /**
     * 转换为 VO
     */
    private NotificationVO convertToVO(SysNotification notification) {
        if (notification == null) {
            return null;
        }

        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);

        // 设置类型标签
        if (notification.getType() != null) {
            NotificationTypeEnum typeEnum = NotificationTypeEnum.fromCode(notification.getType());
            vo.setTypeLabel(typeEnum != null ? typeEnum.getMessage() : null);
        }

        // 设置状态标签
        if (notification.getStatus() != null) {
            NotificationStatusEnum statusEnum = NotificationStatusEnum.fromCode(notification.getStatus());
            vo.setStatusLabel(statusEnum != null ? statusEnum.getMessage() : null);
        }

        return vo;
    }

    /**
     * 批量转换为 VO 列表
     */
    private List<NotificationVO> convertToVOList(List<NotificationVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return new ArrayList<>();
        }

        return voList.stream()
                .map(vo -> {
                    // 设置类型标签
                    if (vo.getType() != null) {
                        NotificationTypeEnum typeEnum = NotificationTypeEnum.fromCode(vo.getType());
                        vo.setTypeLabel(typeEnum != null ? typeEnum.getMessage() : null);
                    }

                    // 设置状态标签
                    if (vo.getStatus() != null) {
                        NotificationStatusEnum statusEnum = NotificationStatusEnum.fromCode(vo.getStatus());
                        vo.setStatusLabel(statusEnum != null ? statusEnum.getMessage() : null);
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }
}

