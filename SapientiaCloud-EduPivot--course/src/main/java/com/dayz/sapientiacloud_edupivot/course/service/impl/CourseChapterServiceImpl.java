package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseChapterConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterAddDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.TeacherVO;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseChapterServiceImpl implements ICourseChapterService {

    private final CourseChapterRepository courseChapterRepository;
    private final MongoTemplate mongoTemplate;
    private final TeacherClient teacherClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO) {
        if (courseChapterQueryDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (courseChapterQueryDTO.getCourseId() != null) {
            criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseChapterQueryDTO.getCourseId());
        }

        if (StringUtils.hasText(courseChapterQueryDTO.getChapterName())) {
            criteria.and(CourseChapterConstants.FIELD_CHAPTER_NAME).regex(courseChapterQueryDTO.getChapterName(), CourseChapterConstants.REGEX_CASE_INSENSITIVE);
        }

        if (courseChapterQueryDTO.getParentChapterId() != null) {
            criteria.and(CourseChapterConstants.FIELD_PARENT_CHAPTER_ID).is(courseChapterQueryDTO.getParentChapterId());
        }

        if (courseChapterQueryDTO.getStatus() != null) {
            criteria.and(CourseChapterConstants.FIELD_STATUS).is(courseChapterQueryDTO.getStatus());
        }

        if (courseChapterQueryDTO.getMinViewCount() != null) {
            criteria.and(CourseChapterConstants.FIELD_VIEW_COUNT).gte(courseChapterQueryDTO.getMinViewCount());
        }
        if (courseChapterQueryDTO.getMaxViewCount() != null) {
            criteria.and(CourseChapterConstants.FIELD_VIEW_COUNT).lte(courseChapterQueryDTO.getMaxViewCount());
        }

        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, CourseChapterConstants.FIELD_SORT_ORDER));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                courseChapterQueryDTO.getPageNum() - CourseChapterConstants.PAGE_NUM_OFFSET,
                courseChapterQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<CourseChapter> chapters = mongoTemplate.find(query, CourseChapter.class);
        long total = mongoTemplate.count(countQuery, CourseChapter.class);

        List<CourseChapterVO> chapterVOList = convertToVO(chapters);
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
    public List<CourseChapterVO> listAllCourseChapterByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, CourseChapterConstants.FIELD_SORT_ORDER));

        List<CourseChapter> chapters = mongoTemplate.find(query, CourseChapter.class);
        return convertToVO(chapters);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'tree:' + #p0", condition = "#p0 != null")
    public List<CourseChapterVO> listCourseChapterTree(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, CourseChapterConstants.FIELD_SORT_ORDER));

        List<CourseChapter> chapters = mongoTemplate.find(query, CourseChapter.class);
        List<CourseChapterVO> chapterVOList = convertToVO(chapters);
        return buildChapterTree(chapterVOList, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "#p0", condition = "#p0 != null")
    public CourseChapterVO getCourseChapterById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_ID).is(id);
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        CourseChapter chapter = mongoTemplate.findOne(query, CourseChapter.class);

        if (chapter == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        return convertToVO(chapter);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseChapter", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "CourseChapter", key = "'tree:' + #p0.courseId")
    })
    public CourseChapterVO addCourseChapter(CourseChapterAddDTO courseChapterAddDTO) {
        if (courseChapterAddDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        if (courseChapterAddDTO.getCourseId() == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        if (!StringUtils.hasText(courseChapterAddDTO.getChapterName())) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_REQUIRED);
        }


        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseChapterAddDTO.getCourseId());
        criteria.and(CourseChapterConstants.FIELD_CHAPTER_NAME).is(courseChapterAddDTO.getChapterName());
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        boolean exists = mongoTemplate.exists(query, CourseChapter.class);

        if (exists) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
        }
        CourseChapter chapter = new CourseChapter();
        BeanUtils.copyProperties(courseChapterAddDTO, chapter);

        chapter.setId(UuidCreator.getTimeOrderedEpoch());

        if (chapter.getStatus() == null) {
            chapter.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (chapter.getViewCount() == null) {
            chapter.setViewCount(CourseChapterConstants.DEFAULT_VIEW_COUNT);
        }
        if (chapter.getLikeCount() == null) {
            chapter.setLikeCount(CourseChapterConstants.DEFAULT_LIKE_COUNT);
        }
        if (chapter.getSortOrder() == null) {
            chapter.setSortOrder(CourseChapterConstants.DEFAULT_SORT_ORDER);
        }
        if (chapter.getDeleted() == null) {
            chapter.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }
        if (chapter.getTeacherId() == null) {
            TeacherVO teacherVO = teacherClient.getTeacherByUserId(UserContextUtil.getCurrentUserId()).getData();
            if (teacherVO != null && teacherVO.getId() != null) {
                chapter.setTeacherId(teacherVO.getId());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        chapter.setCreateTime(now);
        chapter.setUpdateTime(now);

        CourseChapter savedChapter = courseChapterRepository.save(chapter);
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

        CourseChapter existingChapter = courseChapterRepository.findById(courseChapterDTO.getId())
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        if (StringUtils.hasText(courseChapterDTO.getChapterName()) &&
                !courseChapterDTO.getChapterName().equals(existingChapter.getChapterName())) {
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.and(CourseChapterConstants.FIELD_COURSE_ID).is(courseChapterDTO.getCourseId());
            criteria.and(CourseChapterConstants.FIELD_CHAPTER_NAME).is(courseChapterDTO.getChapterName());
            criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

            query.addCriteria(criteria);
            boolean exists = mongoTemplate.exists(query, CourseChapter.class);

            if (exists) {
                throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
            }
        }


        if (StringUtils.hasText(courseChapterDTO.getChapterName())) {
            existingChapter.setChapterName(courseChapterDTO.getChapterName());
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
        if (courseChapterDTO.getAttachmentUrls() != null) {
            existingChapter.setAttachmentUrls(courseChapterDTO.getAttachmentUrls());
        }
        if (courseChapterDTO.getSortOrder() != null) {
            existingChapter.setSortOrder(courseChapterDTO.getSortOrder());
        }
        if (courseChapterDTO.getStatus() != null) {
            existingChapter.setStatus(courseChapterDTO.getStatus());
        }

        existingChapter.setUpdateTime(LocalDateTime.now());

        courseChapterRepository.save(existingChapter);
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

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_PARENT_CHAPTER_ID).is(id);
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        long childCount = mongoTemplate.count(query, CourseChapter.class);

        if (childCount > 0) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_HAS_CHILDREN);
        }

        chapter.setDeleted(DeletedEnum.DELETED.getCode());
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
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

        Query query = new Query(Criteria.where(CourseChapterConstants.FIELD_ID).in(ids));
        Update update = new Update()
                .set(CourseChapterConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(CourseChapterConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        mongoTemplate.updateMulti(query, update, CourseChapter.class);
        return ids.size();
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

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        chapter.setStatus(status);
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
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

        CourseChapter chapter = courseChapterRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS));

        chapter.setSortOrder(sortOrder);
        chapter.setUpdateTime(LocalDateTime.now());
        courseChapterRepository.save(chapter);
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
                Query query = new Query(Criteria.where(CourseChapterConstants.FIELD_ID).is(dto.getId()));
                Update update = new Update()
                        .set(CourseChapterConstants.FIELD_SORT_ORDER, dto.getSortOrder())
                        .set(CourseChapterConstants.FIELD_UPDATE_TIME, LocalDateTime.now());
                mongoTemplate.updateFirst(query, update, CourseChapter.class);
            }
        }
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
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseChapter", key = "'statistics:' + #p0", condition = "#p0 != null")
    public CourseChapterVO getChapterStatistics(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseChapterConstants.FIELD_ID).is(id);
        criteria.and(CourseChapterConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        CourseChapter chapter = mongoTemplate.findOne(query, CourseChapter.class);

        if (chapter == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

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

            if ((parentId == null && currentParentId == null) ||
                    (parentId != null && parentId.equals(currentParentId))) {

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

    /**
     * 批量将PO转换为VO
     *
     * @param chapters 章节PO列表
     * @return 章节VO列表
     */
    private List<CourseChapterVO> convertToVO(List<CourseChapter> chapters) {
        if (CollectionUtils.isEmpty(chapters)) {
            return new ArrayList<>();
        }

        return chapters.stream()
                .map(this::convertToVO)
                .toList();
    }

}
