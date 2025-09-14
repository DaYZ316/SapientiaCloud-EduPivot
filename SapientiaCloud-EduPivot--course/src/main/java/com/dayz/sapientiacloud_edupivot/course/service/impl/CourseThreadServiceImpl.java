package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseThread;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseThreadVO;
import com.dayz.sapientiacloud_edupivot.course.enums.ClosedEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseThreadEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.PinnedEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseThreadMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseThreadService;
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
public class CourseThreadServiceImpl extends ServiceImpl<CourseThreadMapper, CourseThread> implements ICourseThreadService {

    private final CourseThreadMapper courseThreadMapper;

    @Override
    public PageInfo<CourseThreadVO> listCourseThread(CourseThreadQueryDTO courseThreadQueryDTO) {
        if (courseThreadQueryDTO == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_REQUIRED);
        }

        return PageHelper.startPage(courseThreadQueryDTO.getPageNum(), courseThreadQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseThreadMapper.listCourseThread(courseThreadQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseThreadVO> listAllCourseThread() {
        return courseThreadMapper.listAllCourseThread();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseThreadVO getCourseThreadById(UUID threadId) {
        if (threadId == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_REQUIRED);
        }

        CourseThreadVO threadVO = courseThreadMapper.getCourseThreadById(threadId);
        if (threadVO == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        return threadVO;
    }

    @Override
    @Transactional
    public CourseThreadVO addCourseThread(CourseThreadDTO courseThreadDTO) {
        if (courseThreadDTO == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_INFO_REQUIRED);
        }

        if (!StringUtils.hasText(courseThreadDTO.getTitle())) {
            throw new BusinessException(CourseThreadEnum.THREAD_TITLE_REQUIRED);
        }
        if (!StringUtils.hasText(courseThreadDTO.getContent())) {
            throw new BusinessException(CourseThreadEnum.THREAD_CONTENT_REQUIRED);
        }
        if (courseThreadDTO.getCourseId() == null) {
            throw new BusinessException(CourseThreadEnum.COURSE_ID_REQUIRED);
        }
        if (courseThreadDTO.getUserId() == null) {
            throw new BusinessException(CourseThreadEnum.USER_ID_REQUIRED);
        }

        CourseThread courseThread = new CourseThread();
        BeanUtils.copyProperties(courseThreadDTO, courseThread);

        courseThread.setId(UuidCreator.getTimeOrderedEpoch());
        courseThread.setPinned(PinnedEnum.NOT_PINNED.getCode());
        courseThread.setClosed(ClosedEnum.NOT_CLOSED.getCode());
        courseThread.setViewCount(0);
        courseThread.setReplyCount(0);
        courseThread.setLastReplyTime(null);
        courseThread.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        courseThread.setCreateTime(LocalDateTime.now());
        courseThread.setUpdateTime(LocalDateTime.now());

        this.save(courseThread);

        CourseThreadVO courseThreadVO = new CourseThreadVO();
        BeanUtils.copyProperties(courseThread, courseThreadVO);

        return courseThreadVO;
    }

    @Override
    @Transactional
    public Boolean updateCourseThread(CourseThreadDTO courseThreadDTO) {
        if (courseThreadDTO == null || courseThreadDTO.getId() == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_INFO_OR_ID_REQUIRED);
        }

        CourseThread existingThread = this.getById(courseThreadDTO.getId());
        if (existingThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        if (existingThread.getClosed() == 1) {
            throw new BusinessException(CourseThreadEnum.THREAD_IS_CLOSED);
        }

        CourseThread courseThread = new CourseThread();
        BeanUtils.copyProperties(courseThreadDTO, courseThread);
        courseThread.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseThread);
    }

    @Override
    @Transactional
    public Boolean removeCourseThreadById(UUID threadId) {
        if (threadId == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_REQUIRED);
        }

        CourseThread courseThread = this.getById(threadId);
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        courseThread.setDeleted(DeletedEnum.DELETED.getCode());
        courseThread.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseThread);
    }

    @Override
    @Transactional
    public Integer removeCourseThreadByIds(List<UUID> threadIds) {
        if (threadIds == null || threadIds.isEmpty()) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_LIST_REQUIRED);
        }

        // 批量逻辑删除
        int deleteCount = 0;
        for (UUID threadId : threadIds) {
            CourseThread courseThread = this.getById(threadId);
            if (courseThread != null) {
                courseThread.setDeleted(DeletedEnum.DELETED.getCode());
                courseThread.setUpdateTime(LocalDateTime.now());
                if (this.updateById(courseThread)) {
                    deleteCount++;
                }
            }
        }

        return deleteCount;
    }

    @Override
    @Transactional
    public Boolean pinThread(UUID id, Boolean pinned) {
        if (id == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_REQUIRED);
        }
        if (pinned == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_STATUS_INVALID);
        }

        CourseThread courseThread = this.getById(id);
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        courseThread.setPinned(pinned ? 1 : 0);
        courseThread.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseThread);
    }

    @Override
    @Transactional
    public Boolean closeThread(UUID id, Boolean closed) {
        if (id == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_REQUIRED);
        }
        if (closed == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_STATUS_INVALID);
        }

        CourseThread courseThread = this.getById(id);
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        courseThread.setClosed(closed ? 1 : 0);
        courseThread.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseThread);
    }

    @Override
    @Transactional
    public Boolean viewThread(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_ID_REQUIRED);
        }

        CourseThread courseThread = this.getById(id);
        if (courseThread == null) {
            throw new BusinessException(CourseThreadEnum.THREAD_NOT_EXISTS);
        }

        // 增加浏览次数
        courseThread.setViewCount(courseThread.getViewCount() + 1);
        courseThread.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseThread);
    }
}
