package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseThread;
import com.dayz.sapientiacloud_edupivot.course.entity.po.ThreadReply;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ThreadReplyVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseThreadEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.ThreadReplyEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseThreadMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.ThreadReplyMapper;
import com.dayz.sapientiacloud_edupivot.course.service.IThreadReplyService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ThreadReplyServiceImpl extends ServiceImpl<ThreadReplyMapper, ThreadReply> implements IThreadReplyService {

    private final ThreadReplyMapper threadReplyMapper;
    private final CourseThreadMapper courseThreadMapper;

    @Override
    public PageInfo<ThreadReplyVO> listThreadReply(ThreadReplyQueryDTO threadReplyQueryDTO) {
        if (threadReplyQueryDTO == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_REQUIRED);
        }

        return PageHelper.startPage(threadReplyQueryDTO.getPageNum(), threadReplyQueryDTO.getPageSize())
                .doSelectPageInfo(() -> threadReplyMapper.listThreadReply(threadReplyQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThreadReplyVO> listThreadReplyTreeByThreadId(UUID threadId) {
        if (threadId == null) {
            throw new BusinessException(ThreadReplyEnum.THREAD_ID_REQUIRED);
        }

        // 检查主贴是否存在且未关闭
        CourseThread courseThread = courseThreadMapper.selectById(threadId);
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }
        if (courseThread.getClosed() == 1) {
            throw new BusinessException(ThreadReplyEnum.THREAD_IS_CLOSED);
        }

        return threadReplyMapper.listThreadReplyTreeByThreadId(threadId);
    }

    @Override
    @Transactional(readOnly = true)
    public ThreadReplyVO getThreadReplyById(UUID replyId) {
        if (replyId == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_ID_REQUIRED);
        }

        ThreadReplyVO replyVO = threadReplyMapper.getThreadReplyById(replyId);
        if (replyVO == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_NOT_EXISTS);
        }

        return replyVO;
    }

    @Override
    @Transactional
    public ThreadReplyVO addThreadReply(ThreadReplyDTO threadReplyDTO) {
        if (threadReplyDTO == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_INFO_REQUIRED);
        }

        // 验证必填字段
        if (!StringUtils.hasText(threadReplyDTO.getContent())) {
            throw new BusinessException(ThreadReplyEnum.REPLY_CONTENT_REQUIRED);
        }
        if (threadReplyDTO.getThreadId() == null) {
            throw new BusinessException(ThreadReplyEnum.THREAD_ID_REQUIRED);
        }
        if (threadReplyDTO.getUserId() == null) {
            throw new BusinessException(ThreadReplyEnum.USER_ID_REQUIRED);
        }

        // 检查主贴是否存在且未关闭
        CourseThread courseThread = courseThreadMapper.selectById(threadReplyDTO.getThreadId());
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }
        if (courseThread.getClosed() == 1) {
            throw new BusinessException(ThreadReplyEnum.THREAD_IS_CLOSED);
        }

        // 验证父回复是否存在（如果指定了父回复）
        if (threadReplyDTO.getParentReplyId() != null) {
            ThreadReply parentReply = this.getById(threadReplyDTO.getParentReplyId());
            if (parentReply == null) {
                throw new BusinessException(ThreadReplyEnum.PARENT_REPLY_NOT_EXISTS);
            }
            if (!parentReply.getThreadId().equals(threadReplyDTO.getThreadId())) {
                throw new BusinessException(ThreadReplyEnum.PARENT_REPLY_INVALID);
            }
            if (parentReply.getUserId().equals(threadReplyDTO.getUserId())) {
                throw new BusinessException(ThreadReplyEnum.CANNOT_REPLY_TO_SELF);
            }
        }

        ThreadReply threadReply = new ThreadReply();
        BeanUtils.copyProperties(threadReplyDTO, threadReply);

        threadReply.setId(UuidCreator.getTimeOrderedEpoch());
        threadReply.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        threadReply.setCreateTime(LocalDateTime.now());
        threadReply.setUpdateTime(LocalDateTime.now());

        this.save(threadReply);

        // 更新主贴的回复数和最后回复时间
        updateThreadReplyCount(threadReplyDTO.getThreadId());

        ThreadReplyVO threadReplyVO = new ThreadReplyVO();
        BeanUtils.copyProperties(threadReply, threadReplyVO);

        return threadReplyVO;
    }

    @Override
    @Transactional
    public Boolean updateThreadReply(ThreadReplyDTO threadReplyDTO) {
        if (threadReplyDTO == null || threadReplyDTO.getId() == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_INFO_OR_ID_REQUIRED);
        }

        ThreadReply existingReply = this.getById(threadReplyDTO.getId());
        if (existingReply == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_NOT_EXISTS);
        }

        // 检查主贴是否已关闭
        CourseThread courseThread = courseThreadMapper.selectById(existingReply.getThreadId());
        if (courseThread != null && courseThread.getClosed() == 1) {
            throw new BusinessException(ThreadReplyEnum.THREAD_IS_CLOSED);
        }

        ThreadReply threadReply = new ThreadReply();
        BeanUtils.copyProperties(threadReplyDTO, threadReply);
        threadReply.setUpdateTime(LocalDateTime.now());

        return this.updateById(threadReply);
    }

    @Override
    @Transactional
    public Boolean removeThreadReplyById(UUID replyId) {
        if (replyId == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_ID_REQUIRED);
        }

        ThreadReply threadReply = this.getById(replyId);
        if (threadReply == null) {
            throw new BusinessException(ThreadReplyEnum.REPLY_NOT_EXISTS);
        }

        threadReply.setDeleted(DeletedEnum.DELETED.getCode());
        threadReply.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(threadReply);

        // 更新主贴的回复数
        if (result) {
            updateThreadReplyCount(threadReply.getThreadId());
        }

        return result;
    }

    @Override
    @Transactional
    public Integer removeThreadReplyByIds(List<UUID> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) {
            throw new BusinessException(ThreadReplyEnum.REPLY_ID_LIST_REQUIRED);
        }

        // 批量逻辑删除
        int deleteCount = 0;
        UUID threadId = null;
        for (UUID replyId : replyIds) {
            ThreadReply threadReply = this.getById(replyId);
            if (threadReply != null) {
                threadId = threadReply.getThreadId();
                threadReply.setDeleted(DeletedEnum.DELETED.getCode());
                threadReply.setUpdateTime(LocalDateTime.now());
                if (this.updateById(threadReply)) {
                    deleteCount++;
                }
            }
        }

        // 更新主贴的回复数
        if (threadId != null) {
            updateThreadReplyCount(threadId);
        }

        return deleteCount;
    }

    /**
     * 更新主贴的回复数和最后回复时间
     */
    private void updateThreadReplyCount(UUID threadId) {
        // 统计有效回复数
        LambdaQueryWrapper<ThreadReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ThreadReply::getThreadId, threadId)
                .eq(ThreadReply::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        long replyCount = this.count(queryWrapper);

        // 获取最后回复时间
        queryWrapper.orderByDesc(ThreadReply::getCreateTime);
        queryWrapper.last("LIMIT 1");
        ThreadReply lastReply = this.getOne(queryWrapper);
        LocalDateTime lastReplyTime = lastReply != null ? lastReply.getCreateTime() : null;

        // 更新主贴
        CourseThread courseThread = new CourseThread();
        courseThread.setId(threadId);
        courseThread.setReplyCount((int) replyCount);
        courseThread.setLastReplyTime(lastReplyTime);
        courseThread.setUpdateTime(LocalDateTime.now());

        courseThreadMapper.updateById(courseThread);
    }
}
