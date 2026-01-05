package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.system.common.clients.vo.CourseStudentClientVO;
import com.dayz.sapientiacloud_edupivot.system.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationBatchAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationScopeSendDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotificationMsg;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotificationUser;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationStatusEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationTargetScopeEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.NotificationTypeEnum;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysNotificationMsgMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysNotificationUserMapper;
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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMsgMapper, SysNotificationMsg> implements ISysNotificationService {

    private final SysNotificationMsgMapper sysNotificationMsgMapper;
    private final SysNotificationUserMapper sysNotificationUserMapper;
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
        List<NotificationVO> list = sysNotificationUserMapper.listNotificationVO(queryDTO);
        PageInfo<NotificationVO> pageInfo = new PageInfo<>(convertToVOList(list));
        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationVO> listAllNotificationByUserId(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        NotificationQueryDTO queryDTO = new NotificationQueryDTO();
        queryDTO.setUserId(userId);
        
        List<NotificationVO> list = sysNotificationUserMapper.listNotificationVO(queryDTO);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        LambdaQueryWrapper<SysNotificationUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotificationUser::getUserId, userId)
                .eq(SysNotificationUser::getStatus, NotificationStatusEnum.UNREAD.getCode());

        return sysNotificationUserMapper.selectCount(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationVO getNotificationById(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }
        
        // 这里的ID是用户收件箱ID
        SysNotificationUser notificationUser = sysNotificationUserMapper.selectById(id);
        if (notificationUser == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }
        
        SysNotificationMsg notificationMsg = sysNotificationMsgMapper.selectById(notificationUser.getNotificationId());
        if (notificationMsg == null || (notificationMsg.getDeleted() != null && notificationMsg.getDeleted().equals(DeletedEnum.DELETED.getCode()))) {
             throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        return combineToVO(notificationMsg, notificationUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeNotificationMsg(UUID msgId) {
        if (msgId == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotificationMsg msg = sysNotificationMsgMapper.selectById(msgId);
        if (msg == null || (msg.getDeleted() != null && Integer.valueOf(DeletedEnum.DELETED.getCode()).equals(msg.getDeleted()))) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }
        // 使用显式的 UpdateWrapper 指定要更新的列，避免实体映射/更新策略导致字段未被持久化的问题
        UpdateWrapper<SysNotificationMsg> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", msgId)
                     .set("is_deleted", DeletedEnum.DELETED.getCode())
                     .set("update_time", LocalDateTime.now());

        int rows = sysNotificationMsgMapper.update(null, updateWrapper);
        return rows > 0;
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

        // 1. 保存消息内容
        SysNotificationMsg msg = new SysNotificationMsg();
        msg.setId(UuidCreator.getTimeOrderedEpoch());
        msg.setTitle(addDTO.getTitle());
        msg.setContent(addDTO.getContent());
        msg.setAttachmentUrls(addDTO.getAttachmentUrls());
        msg.setType(addDTO.getType() != null ? addDTO.getType() : NotificationTypeEnum.SYSTEM.getCode());
        msg.setSenderId(addDTO.getSenderId());
        msg.setSenderName(addDTO.getSenderName());
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        msg.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        
        this.save(msg);
        
        // 2. 保存用户关联
        SysNotificationUser user = new SysNotificationUser();
        user.setId(UuidCreator.getTimeOrderedEpoch());
        user.setNotificationId(msg.getId());
        user.setUserId(addDTO.getUserId());
        user.setStatus(NotificationStatusEnum.UNREAD.getCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        sysNotificationUserMapper.insert(user);
        
        return combineToVO(msg, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchAddNotification(NotificationBatchAddDTO batchAddDTO) {
        if (batchAddDTO == null) {
            throw new BusinessException(NotificationEnum.DATA_CANNOT_BE_EMPTY);
        }

        List<UUID> userIds = batchAddDTO.getUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        if (!StringUtils.hasText(batchAddDTO.getTitle())) {
            throw new BusinessException(NotificationEnum.TITLE_REQUIRED);
        }

        if (batchAddDTO.getType() != null && !NotificationTypeEnum.isValidCode(batchAddDTO.getType())) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_TYPE_INVALID);
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 保存消息内容 (只存一条)
        SysNotificationMsg msg = new SysNotificationMsg();
        msg.setId(UuidCreator.getTimeOrderedEpoch());
        msg.setTitle(batchAddDTO.getTitle());
        msg.setContent(batchAddDTO.getContent());
        msg.setAttachmentUrls(batchAddDTO.getAttachmentUrls());
        msg.setType(batchAddDTO.getType() != null ? batchAddDTO.getType() : NotificationTypeEnum.SYSTEM.getCode());
        msg.setSenderId(batchAddDTO.getSenderId());
        msg.setSenderName(batchAddDTO.getSenderName());
        msg.setCreateTime(now);
        msg.setUpdateTime(now);
        msg.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        
        this.save(msg);

        // 2. 批量保存用户关联
        // 由于MyBatis-Plus的saveBatch可能较慢，这里使用循环插入，或者可以在Mapper中写批量插入SQL优化
        // 考虑到这里是业务层，先用循环插入，如果性能有瓶颈再优化
        int successCount = 0;
        for (UUID userId : userIds) {
            SysNotificationUser user = new SysNotificationUser();
            user.setId(UuidCreator.getTimeOrderedEpoch());
            user.setNotificationId(msg.getId());
            user.setUserId(userId);
            user.setStatus(NotificationStatusEnum.UNREAD.getCode());
            user.setCreateTime(now);
            user.setUpdateTime(now);
            
            sysNotificationUserMapper.insert(user);
            successCount++;
        }

        return successCount;
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

        NotificationBatchAddDTO batchAddDTO = new NotificationBatchAddDTO();
        batchAddDTO.setUserIds(targetUserIds);
        batchAddDTO.setTitle(scopeSendDTO.getTitle());
        batchAddDTO.setContent(scopeSendDTO.getContent());
        batchAddDTO.setType(scopeSendDTO.getType());
        batchAddDTO.setSenderId(scopeSendDTO.getSenderId());
        batchAddDTO.setSenderName(scopeSendDTO.getSenderName());
        batchAddDTO.setAttachmentUrls(scopeSendDTO.getAttachmentUrls());

        return batchAddNotification(batchAddDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateNotification(NotificationDTO notificationDTO) {
        if (notificationDTO == null || notificationDTO.getId() == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }
        
        // 注意：这里的ID应该是MsgID，但如果是从列表点进去编辑，前端传的可能是MsgID
        // 假设这里更新的是Msg内容，那么需要传入的是MsgID。
        // 如果业务场景是用户修改自己的备注之类的，那是UserId。
        // 通常后台管理更新通知，是更新Msg。
        
        SysNotificationMsg msg = this.getById(notificationDTO.getId());
        if (msg == null || (msg.getDeleted() != null && Integer.valueOf(DeletedEnum.DELETED.getCode()).equals(msg.getDeleted()))) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        if (StringUtils.hasText(notificationDTO.getTitle())) {
            msg.setTitle(notificationDTO.getTitle());
        }

        if (notificationDTO.getContent() != null) {
            msg.setContent(notificationDTO.getContent());
        }

        if (notificationDTO.getAttachmentUrls() != null) {
            msg.setAttachmentUrls(notificationDTO.getAttachmentUrls());
        }

        if (notificationDTO.getType() != null) {
            if (!NotificationTypeEnum.isValidCode(notificationDTO.getType())) {
                throw new BusinessException(NotificationEnum.NOTIFICATION_TYPE_INVALID);
            }
            msg.setType(notificationDTO.getType());
        }

        msg.setUpdateTime(LocalDateTime.now());

        return this.updateById(msg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markAsRead(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        SysNotificationUser user = sysNotificationUserMapper.selectById(id);
        if (user == null) {
             throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        user.setStatus(NotificationStatusEnum.READ.getCode());
        user.setReadTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return sysNotificationUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchMarkAsRead(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_IDS_REQUIRED);
        }

        List<SysNotificationUser> userList = sysNotificationUserMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(userList)) {
             throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;

        for (SysNotificationUser user : userList) {
            user.setStatus(NotificationStatusEnum.READ.getCode());
            user.setReadTime(now);
            user.setUpdateTime(now);
            sysNotificationUserMapper.updateById(user);
            successCount++;
        }

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer markAllAsRead(UUID userId) {
        if (userId == null) {
            throw new BusinessException(NotificationEnum.USER_ID_REQUIRED);
        }

        LambdaQueryWrapper<SysNotificationUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotificationUser::getUserId, userId)
                .eq(SysNotificationUser::getStatus, NotificationStatusEnum.UNREAD.getCode());

        List<SysNotificationUser> userList = sysNotificationUserMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(userList)) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        for (SysNotificationUser user : userList) {
            user.setStatus(NotificationStatusEnum.READ.getCode());
            user.setReadTime(now);
            user.setUpdateTime(now);
            sysNotificationUserMapper.updateById(user);
        }

        return userList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeNotificationById(UUID id) {
        if (id == null) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_ID_REQUIRED);
        }

        // 物理删除
        boolean success = sysNotificationUserMapper.deleteById(id) > 0;
        if (!success) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer removeNotificationByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_IDS_REQUIRED);
        }
        
        // 物理删除
        int count = sysNotificationUserMapper.deleteBatchIds(ids);
        if (count == 0) {
            throw new BusinessException(NotificationEnum.NOTIFICATION_NOT_EXISTS);
        }
        return count;
    }
    
    /**
     * 组合 Msg 和 User 为 VO
     */
    private NotificationVO combineToVO(SysNotificationMsg msg, SysNotificationUser user) {
        if (msg == null) return null;
        
        NotificationVO vo = new NotificationVO();
        // 复制 msg 属性
        BeanUtils.copyProperties(msg, vo);
        // 覆盖 id 为 user 表的 id (收件记录ID)
        vo.setId(user.getId());
        vo.setUserId(user.getUserId());
        vo.setStatus(user.getStatus());
        vo.setReadTime(user.getReadTime());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        
        // 设置类型标签
        if (msg.getType() != null) {
            NotificationTypeEnum typeEnum = NotificationTypeEnum.fromCode(msg.getType());
            vo.setTypeLabel(typeEnum != null ? typeEnum.getMessage() : null);
        }

        // 设置状态标签
        if (user.getStatus() != null) {
            NotificationStatusEnum statusEnum = NotificationStatusEnum.fromCode(user.getStatus());
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
