package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseChapterConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseChapterEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.CourseChapterRepository;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseChapterService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
@Slf4j
@RequiredArgsConstructor
public class CourseChapterServiceImpl implements ICourseChapterService {

    private final CourseChapterRepository courseChapterRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO) {
        if (courseChapterQueryDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 课程ID
        if (courseChapterQueryDTO.getCourseId() != null) {
            criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseChapterQueryDTO.getCourseId());
        }

        // 章节名称模糊查询
        if (StringUtils.hasText(courseChapterQueryDTO.getChapterName())) {
            criteria.and(CourseChapterConstants.FIELD_CHAPTER_NAME).regex(courseChapterQueryDTO.getChapterName(), CourseChapterConstants.REGEX_CASE_INSENSITIVE);
        }

        // 父章节ID
        if (courseChapterQueryDTO.getParentChapterId() != null) {
            criteria.and(CourseChapterConstants.FIELD_PARENT_CHAPTER_ID).is(courseChapterQueryDTO.getParentChapterId());
        }

        // 状态
        if (courseChapterQueryDTO.getStatus() != null) {
            criteria.and(CourseChapterConstants.FIELD_STATUS).is(courseChapterQueryDTO.getStatus());
        }

        // 浏览次数范围
        if (courseChapterQueryDTO.getMinViewCount() != null) {
            criteria.and(CourseChapterConstants.FIELD_VIEW_COUNT).gte(courseChapterQueryDTO.getMinViewCount());
        }
        if (courseChapterQueryDTO.getMaxViewCount() != null) {
            criteria.and(CourseChapterConstants.FIELD_VIEW_COUNT).lte(courseChapterQueryDTO.getMaxViewCount());
        }

        // 逻辑删除
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, CourseChapterConstants.FIELD_SORT_ORDER, CourseChapterConstants.FIELD_CHAPTER_NUMBER));

        // 分页
        Pageable pageable = PageRequest.of(
                courseChapterQueryDTO.getPageNum() - CourseChapterConstants.PAGE_NUM_OFFSET,
                courseChapterQueryDTO.getPageSize()
        );
        query.with(pageable);

        // 执行查询
        List<CourseChapter> chapters = mongoTemplate.find(query, CourseChapter.class);
        long total = mongoTemplate.count(query, CourseChapter.class);

        // 转换为VO
        List<CourseChapterVO> chapterVOList = chapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页信息
        PageInfo<CourseChapterVO> pageInfo = new PageInfo<>(chapterVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(courseChapterQueryDTO.getPageNum());
        pageInfo.setPageSize(courseChapterQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / courseChapterQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'course:' + #p0", condition = "#p0 != null")
    public List<CourseChapterVO> listCourseChapterByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        return chapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'tree:' + #p0", condition = "#p0 != null")
    public List<CourseChapterVO> listCourseChapterTree(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        // 获取所有章节
        List<CourseChapter> allChapters = courseChapterRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        
        // 转换为VO
        List<CourseChapterVO> allChapterVOs = allChapters.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildChapterTree(allChapterVOs, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "#p0", condition = "#p0 != null")
    public CourseChapterVO getCourseChapterById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        return convertToVO(chapter);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "CourseChapter", key = "'tree:' + #p0.courseId")
    })
    public CourseChapterVO addCourseChapter(CourseChapterDTO courseChapterDTO) {
        if (courseChapterDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        // 验证课程ID
        if (courseChapterDTO.getCourseId() == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        // 验证章节名称
        if (!StringUtils.hasText(courseChapterDTO.getChapterName())) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_REQUIRED);
        }

        // 检查章节名称是否重复
        if (courseChapterRepository.existsByCourseIdAndChapterName(
                courseChapterDTO.getCourseId(), courseChapterDTO.getChapterName())) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
        }

        // 检查章节序号是否重复
        if (courseChapterDTO.getChapterNumber() != null &&
                courseChapterRepository.existsByCourseIdAndChapterNumber(
                        courseChapterDTO.getCourseId(), courseChapterDTO.getChapterNumber())) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
        }

        // 创建章节实体
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(courseChapterDTO, chapter);
        
        // 设置ID
        chapter.setId(UuidCreator.getTimeOrderedEpoch());
        
        // 设置默认值
        if (chapter.getStatus() == null) {
            chapter.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (chapter.getViewCount() == null) {
            chapter.setViewCount(CourseChapterConstants.DEFAULT_VIEW_COUNT);
        }
        if (chapter.getLikeCount() == null) {
            chapter.setLikeCount(CourseChapterConstants.DEFAULT_LIKE_COUNT);
        }
        if (chapter.getCommentCount() == null) {
            chapter.setCommentCount(CourseChapterConstants.DEFAULT_COMMENT_COUNT);
        }
        if (chapter.getSortOrder() == null) {
            chapter.setSortOrder(CourseChapterConstants.DEFAULT_SORT_ORDER);
        }
        if (chapter.getDeleted() == null) {
            chapter.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        chapter.setCreateTime(now);
        chapter.setUpdateTime(now);

        // 保存章节
        CourseChapter savedChapter = courseChapterRepository.save(chapter);
        
        log.info(CourseChapterConstants.LOG_ADD_SUCCESS, savedChapter.getChapterName());
        return convertToVO(savedChapter);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0.id"),
            @CacheEvict(value = "CourseChapter", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "CourseChapter", key = "'tree:' + #p0.courseId")
    })
    public Boolean updateCourseChapter(CourseChapterDTO courseChapterDTO) {
        if (courseChapterDTO == null || courseChapterDTO.getId() == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_INFO_OR_ID_REQUIRED);
        }

        // 检查章节是否存在
        CourseChapter existingChapter = courseChapterRepository.findById(courseChapterDTO.getId())
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        // 验证章节名称是否重复（排除自己）
        if (StringUtils.hasText(courseChapterDTO.getChapterName()) &&
                !courseChapterDTO.getChapterName().equals(existingChapter.getChapterName())) {
            if (courseChapterRepository.existsByCourseIdAndChapterName(
                    courseChapterDTO.getCourseId(), courseChapterDTO.getChapterName())) {
                throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
            }
        }

        // 验证章节序号是否重复（排除自己）
        if (courseChapterDTO.getChapterNumber() != null &&
                !courseChapterDTO.getChapterNumber().equals(existingChapter.getChapterNumber())) {
            if (courseChapterRepository.existsByCourseIdAndChapterNumber(
                    courseChapterDTO.getCourseId(), courseChapterDTO.getChapterNumber())) {
                throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
            }
        }

        // 更新字段
        if (StringUtils.hasText(courseChapterDTO.getChapterName())) {
            existingChapter.setChapterName(courseChapterDTO.getChapterName());
        }
        if (courseChapterDTO.getChapterNumber() != null) {
            existingChapter.setChapterNumber(courseChapterDTO.getChapterNumber());
        }
        if (courseChapterDTO.getParentChapterId() != null) {
            existingChapter.setParentChapterId(courseChapterDTO.getParentChapterId());
        }
        if (courseChapterDTO.getDescription() != null) {
            existingChapter.setDescription(courseChapterDTO.getDescription());
        }
        if (courseChapterDTO.getContent() != null) {
            existingChapter.setContent(courseChapterDTO.getContent());
        }
        if (courseChapterDTO.getVideoUrl() != null) {
            existingChapter.setVideoUrl(courseChapterDTO.getVideoUrl());
        }
        if (courseChapterDTO.getVideoDuration() != null) {
            existingChapter.setVideoDuration(courseChapterDTO.getVideoDuration());
        }
        if (courseChapterDTO.getAttachmentUrls() != null) {
            existingChapter.setAttachmentUrls(courseChapterDTO.getAttachmentUrls());
        }
        if (courseChapterDTO.getSortOrder() != null) {
            existingChapter.setSortOrder(courseChapterDTO.getSortOrder());
        }
        if (courseChapterDTO.getStatus() != null) {
            existingChapter.setStatus(courseChapterDTO.getStatus());
        }

        // 更新时间
        existingChapter.setUpdateTime(LocalDateTime.now());

        // 保存更新
        courseChapterRepository.save(existingChapter);
        
        log.info(CourseChapterConstants.LOG_UPDATE_SUCCESS, existingChapter.getChapterName());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0"),
            @CacheEvict(value = "CourseChapter", allEntries = true)
    })
    public Boolean removeCourseChapterById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        // 检查章节是否存在
        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        // 检查是否有子章节
        long childCount = courseChapterRepository.countByParentChapterId(id);
        if (childCount > 0) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_HAS_CHILDREN);
        }

        // 逻辑删除
        chapter.setDeleted(DeletedEnum.DELETED.getCode());
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
        
        log.info(CourseChapterConstants.LOG_DELETE_SUCCESS, chapter.getChapterName());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", allEntries = true)
    })
    public Integer removeCourseChapterByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_LIST_REQUIRED);
        }

        int deletedCount = 0;
        for (UUID id : ids) {
            try {
                if (removeCourseChapterById(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn(CourseChapterConstants.LOG_DELETE_FAILED, id, e.getMessage());
            }
        }

        log.info(CourseChapterConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0"),
            @CacheEvict(value = "CourseChapter", allEntries = true)
    })
    public Boolean updateChapterStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }
        if (status == null || (status < CourseChapterConstants.STATUS_MIN || status > CourseChapterConstants.STATUS_MAX)) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_STATUS_INVALID);
        }

        // 检查章节是否存在
        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        // 更新状态
        chapter.setStatus(status);
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
        
        log.info(CourseChapterConstants.LOG_UPDATE_STATUS_SUCCESS, chapter.getChapterName(), status);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0"),
            @CacheEvict(value = "CourseChapter", allEntries = true)
    })
    public Boolean updateChapterSortOrder(UUID id, Integer sortOrder) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }
        if (sortOrder == null || sortOrder < 0) {
            throw new BusinessException(CourseChapterEnum.SORT_ORDER_INVALID);
        }

        // 检查章节是否存在
        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        // 更新排序
        chapter.setSortOrder(sortOrder);
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
        
        log.info(CourseChapterConstants.LOG_UPDATE_SORT_SUCCESS, chapter.getChapterName(), sortOrder);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", allEntries = true)
    })
    public Boolean batchUpdateChapterSortOrder(List<CourseChapterDTO> chapterSortList) {
        if (CollectionUtils.isEmpty(chapterSortList)) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_LIST_REQUIRED);
        }

        for (CourseChapterDTO dto : chapterSortList) {
            if (dto.getId() != null && dto.getSortOrder() != null) {
                updateChapterSortOrder(dto.getId(), dto.getSortOrder());
            }
        }

        log.info(CourseChapterConstants.LOG_BATCH_UPDATE_SORT_SUCCESS, chapterSortList.size());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0")
    })
    public Boolean likeChapter(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(CourseChapterConstants.FIELD_ID).is(id));
        Update update = new Update().inc(CourseChapterConstants.FIELD_LIKE_COUNT, CourseChapterConstants.INCREMENT_VALUE);
        
        mongoTemplate.updateFirst(query, update, CourseChapter.class);
        
        log.info(CourseChapterConstants.LOG_LIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0")
    })
    public Boolean unlikeChapter(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(CourseChapterConstants.FIELD_ID).is(id));
        Update update = new Update().inc(CourseChapterConstants.FIELD_LIKE_COUNT, CourseChapterConstants.DECREMENT_VALUE);
        
        mongoTemplate.updateFirst(query, update, CourseChapter.class);
        
        log.info(CourseChapterConstants.LOG_UNLIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "#p0")
    })
    public Boolean viewChapter(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(CourseChapterConstants.FIELD_ID).is(id));
        Update update = new Update().inc(CourseChapterConstants.FIELD_VIEW_COUNT, CourseChapterConstants.INCREMENT_VALUE);
        
        mongoTemplate.updateFirst(query, update, CourseChapter.class);
        
        log.info(CourseChapterConstants.LOG_VIEW_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'statistics:' + #p0", condition = "#p0 != null")
    public CourseChapterVO getChapterStatistics(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        return convertToVO(chapter);
    }

    /**
     * 构建章节树形结构
     *
     * @param allChapters 所有章节
     * @param parentId    父章节ID
     * @return 树形结构章节列表
     */
    private List<CourseChapterVO> buildChapterTree(List<CourseChapterVO> allChapters, UUID parentId) {
        List<CourseChapterVO> result = new ArrayList<>();
        
        for (CourseChapterVO chapter : allChapters) {
            UUID currentParentId = chapter.getParentChapterId();
            
            // 判断是否为当前层级的章节
            if ((parentId == null && currentParentId == null) ||
                (parentId != null && parentId.equals(currentParentId))) {
                
                // 递归查找子章节
                List<CourseChapterVO> children = buildChapterTree(allChapters, chapter.getId());
                chapter.setChildren(children);
                
                result.add(chapter);
            }
        }
        
        return result;
    }

    /**
     * 将PO转换为VO
     *
     * @param chapter 章节PO
     * @return 章节VO
     */
    private CourseChapterVO convertToVO(CourseChapter chapter) {
        if (chapter == null) {
            return null;
        }

        CourseChapterVO vo = new CourseChapterVO();
        BeanUtils.copyProperties(chapter, vo);
        return vo;
    }
}
