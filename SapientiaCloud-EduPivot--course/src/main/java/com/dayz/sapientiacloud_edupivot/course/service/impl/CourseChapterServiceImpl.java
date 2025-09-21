package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseChapterEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseChapterMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseChapterService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements ICourseChapterService {

    private final CourseChapterMapper courseChapterMapper;

    @Override
    public PageInfo<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO) {
        if (courseChapterQueryDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        return PageHelper.startPage(courseChapterQueryDTO.getPageNum(), courseChapterQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseChapterMapper.listCourseChapter(courseChapterQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'tree_' + #p0", condition = "#p0 != null")
    public List<CourseChapterVO> listAllCourseChapterTree(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        return courseChapterMapper.listAllCourseChapterTree(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseChapterVO getCourseChapterById(UUID chapterId) {
        if (chapterId == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        CourseChapterVO chapterVO = courseChapterMapper.getCourseChapterById(chapterId);
        if (chapterVO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        return chapterVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseChapterVO addCourseChapter(CourseChapterDTO courseChapterDTO) {
        if (courseChapterDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_INFO_REQUIRED);
        }

        // 验证必填字段
        if (!StringUtils.hasText(courseChapterDTO.getChapterName())) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_TITLE_REQUIRED);
        }
        if (courseChapterDTO.getSort() == null || courseChapterDTO.getSort() <= 0) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_SORT_INVALID);
        }

        // 检查章节名称是否重复（同一课程下）
        LambdaQueryWrapper<CourseChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseChapter::getCourseId, courseChapterDTO.getCourseId())
                .eq(CourseChapter::getChapterName, courseChapterDTO.getChapterName())
                .eq(CourseChapter::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
        }

        // 验证父章节是否存在
        if (courseChapterDTO.getParentId() != null) {
            CourseChapter parentChapter = this.getById(courseChapterDTO.getParentId());
            if (parentChapter == null || !parentChapter.getCourseId().equals(courseChapterDTO.getCourseId())) {
                throw new BusinessException(CourseChapterEnum.PARENT_CHAPTER_NOT_EXISTS);
            }
        }

        CourseChapter courseChapter = new CourseChapter();
        BeanUtils.copyProperties(courseChapterDTO, courseChapter);

        courseChapter.setId(UuidCreator.getTimeOrderedEpoch());
        courseChapter.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        courseChapter.setCreateTime(LocalDateTime.now());
        courseChapter.setUpdateTime(LocalDateTime.now());

        this.save(courseChapter);

        CourseChapterVO courseChapterVO = new CourseChapterVO();
        BeanUtils.copyProperties(courseChapter, courseChapterVO);

        return courseChapterVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateCourseChapter(CourseChapterDTO courseChapterDTO) {
        if (courseChapterDTO == null || courseChapterDTO.getId() == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_INFO_OR_ID_REQUIRED);
        }

        CourseChapter existingChapter = this.getById(courseChapterDTO.getId());
        if (existingChapter == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        // 检查章节名称是否重复（排除自身）
        if (StringUtils.hasText(courseChapterDTO.getChapterName()) &&
                !courseChapterDTO.getChapterName().equals(existingChapter.getChapterName())) {
            LambdaQueryWrapper<CourseChapter> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseChapter::getCourseId, courseChapterDTO.getCourseId())
                    .eq(CourseChapter::getChapterName, courseChapterDTO.getChapterName())
                    .ne(CourseChapter::getId, courseChapterDTO.getId())
                    .eq(CourseChapter::getDeleted, DeletedEnum.NOT_DELETED.getCode());
            if (this.count(queryWrapper) > 0) {
                throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
            }
        }

        // 验证父章节是否存在且不能是自身或子章节
        if (courseChapterDTO.getParentId() != null) {
            if (courseChapterDTO.getParentId().equals(courseChapterDTO.getId())) {
                throw new BusinessException(CourseChapterEnum.CANNOT_SET_SELF_AS_PARENT);
            }

            CourseChapter parentChapter = this.getById(courseChapterDTO.getParentId());
            if (parentChapter == null || !parentChapter.getCourseId().equals(courseChapterDTO.getCourseId())) {
                throw new BusinessException(CourseChapterEnum.PARENT_CHAPTER_NOT_EXISTS);
            }

            // 检查是否会造成循环引用（简单检查，实际项目中可能需要更复杂的递归检查）
            if (isChildChapter(courseChapterDTO.getId(), courseChapterDTO.getParentId())) {
                throw new BusinessException(CourseChapterEnum.CANNOT_SET_CHILD_AS_PARENT);
            }
        }

        CourseChapter courseChapter = new CourseChapter();
        BeanUtils.copyProperties(courseChapterDTO, courseChapter);
        courseChapter.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseChapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeCourseChapterById(UUID chapterId) {
        if (chapterId == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        return courseChapterMapper.removeCourseChapterById(chapterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer removeCourseChapterByIds(List<UUID> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_LIST_REQUIRED);
        }

        return courseChapterMapper.removeCourseChapterByIds(chapterIds);
    }

    /**
     * 检查chapterId是否是parentId的子章节
     */
    private boolean isChildChapter(UUID chapterId, UUID parentId) {
        LambdaQueryWrapper<CourseChapter> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseChapter::getParentId, parentId)
                .eq(CourseChapter::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        List<CourseChapter> children = this.list(queryWrapper);

        for (CourseChapter child : children) {
            if (child.getId().equals(chapterId)) {
                return true;
            }
            // 递归检查子章节的子章节
            if (isChildChapter(chapterId, child.getId())) {
                return true;
            }
        }

        return false;
    }
}
