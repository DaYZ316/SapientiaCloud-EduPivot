package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseForumConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseForum;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseForumVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseForumEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.CourseForumRepository;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseForumService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.result.UpdateResult;
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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseForumServiceImpl implements ICourseForumService {

    private final CourseForumRepository courseForumRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseForumVO> listCourseForum(CourseForumQueryDTO courseForumQueryDTO) {
        if (courseForumQueryDTO == null) {
            throw new BusinessException(CourseForumEnum.FORUM_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (courseForumQueryDTO.getCourseId() != null) {
            criteria.and(CourseForumConstants.FIELD_COURSE_ID).is(courseForumQueryDTO.getCourseId());
        }

        if (StringUtils.hasText(courseForumQueryDTO.getForumName())) {
            criteria.and(CourseForumConstants.FIELD_FORUM_NAME).regex(courseForumQueryDTO.getForumName(), CourseForumConstants.REGEX_CASE_INSENSITIVE);
        }

        if (courseForumQueryDTO.getForumType() != null) {
            criteria.and(CourseForumConstants.FIELD_FORUM_TYPE).is(courseForumQueryDTO.getForumType());
        }

        if (courseForumQueryDTO.getStatus() != null) {
            criteria.and(CourseForumConstants.FIELD_STATUS).is(courseForumQueryDTO.getStatus());
        }

        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseForumConstants.FIELD_CREATE_TIME));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                courseForumQueryDTO.getPageNum() - CourseForumConstants.PAGE_NUM_OFFSET,
                courseForumQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<CourseForum> forums = mongoTemplate.find(query, CourseForum.class);
        long total = mongoTemplate.count(countQuery, CourseForum.class);

        List<CourseForumVO> forumVOList = forums.stream()
                .map(this::convertToVO)
                .toList();

        PageInfo<CourseForumVO> pageInfo = new PageInfo<>(forumVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(courseForumQueryDTO.getPageNum());
        pageInfo.setPageSize(courseForumQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / courseForumQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseForum", key = "'course:' + #p0", condition = "#p0 != null")
    public List<CourseForumVO> listAllCourseForumByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseForumEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseForumConstants.FIELD_CREATE_TIME));

        List<CourseForum> forums = mongoTemplate.find(query, CourseForum.class);
        return forums.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseForum", key = "#p0", condition = "#p0 != null")
    public CourseForumVO getCourseForumById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_ID).is(id);
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseForum forum = mongoTemplate.findOne(query, CourseForum.class);
        if (forum == null) {
            throw new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS);
        }

        return convertToVO(forum);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", key = "'course:' + #p0.courseId")
    })
    public CourseForumVO addCourseForum(CourseForumDTO courseForumDTO) {
        if (courseForumDTO == null) {
            throw new BusinessException(CourseForumEnum.FORUM_REQUIRED);
        }

        if (courseForumDTO.getCourseId() == null) {
            throw new BusinessException(CourseForumEnum.COURSE_ID_REQUIRED);
        }

        if (!StringUtils.hasText(courseForumDTO.getForumName())) {
            throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_COURSE_ID).is(courseForumDTO.getCourseId());
        criteria.and(CourseForumConstants.FIELD_FORUM_NAME).is(courseForumDTO.getForumName());
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        if (mongoTemplate.exists(query, CourseForum.class)) {
            throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
        }

        CourseForum forum = new CourseForum();
        BeanUtils.copyProperties(courseForumDTO, forum);

        forum.setId(UuidCreator.getTimeOrderedEpoch());

        if (forum.getStatus() == null) {
            forum.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (forum.getPostCount() == null) {
            forum.setPostCount(CourseForumConstants.DEFAULT_POST_COUNT);
        }
        if (forum.getReplyCount() == null) {
            forum.setReplyCount(CourseForumConstants.DEFAULT_REPLY_COUNT);
        }
        if (forum.getDeleted() == null) {
            forum.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        LocalDateTime now = LocalDateTime.now();
        forum.setCreateTime(now);
        forum.setUpdateTime(now);

        CourseForum savedForum = courseForumRepository.save(forum);

        log.info(CourseForumConstants.LOG_ADD_SUCCESS, savedForum.getForumName());
        return convertToVO(savedForum);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", key = "#p0.id"),
            @CacheEvict(value = "CourseForum", key = "'course:' + #p0.courseId")
    })
    public Boolean updateCourseForum(CourseForumDTO courseForumDTO) {
        if (courseForumDTO == null || courseForumDTO.getId() == null) {
            throw new BusinessException(CourseForumEnum.FORUM_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_ID).is(courseForumDTO.getId());
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseForum existingForum = mongoTemplate.findOne(query, CourseForum.class);
        if (existingForum == null) {
            throw new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS);
        }

        if (StringUtils.hasText(courseForumDTO.getForumName()) &&
                !courseForumDTO.getForumName().equals(existingForum.getForumName())) {
            Query nameCheckQuery = new Query();
            Criteria nameCheckCriteria = new Criteria();
            nameCheckCriteria.and(CourseForumConstants.FIELD_COURSE_ID).is(courseForumDTO.getCourseId());
            nameCheckCriteria.and(CourseForumConstants.FIELD_FORUM_NAME).is(courseForumDTO.getForumName());
            nameCheckCriteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
            nameCheckQuery.addCriteria(nameCheckCriteria);

            if (mongoTemplate.exists(nameCheckQuery, CourseForum.class)) {
                throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
            }
        }

        if (StringUtils.hasText(courseForumDTO.getForumName())) {
            existingForum.setForumName(courseForumDTO.getForumName());
        }
        if (courseForumDTO.getDescription() != null) {
            existingForum.setDescription(courseForumDTO.getDescription());
        }
        if (courseForumDTO.getForumType() != null) {
            existingForum.setForumType(courseForumDTO.getForumType());
        }
        if (courseForumDTO.getIsPublic() != null) {
            existingForum.setIsPublic(courseForumDTO.getIsPublic());
        }
        if (courseForumDTO.getAllowAnonymous() != null) {
            existingForum.setAllowAnonymous(courseForumDTO.getAllowAnonymous());
        }
        if (courseForumDTO.getStatus() != null) {
            existingForum.setStatus(courseForumDTO.getStatus());
        }
        if (courseForumDTO.getTags() != null) {
            existingForum.setTags(courseForumDTO.getTags());
        }

        existingForum.setUpdateTime(LocalDateTime.now());

        courseForumRepository.save(existingForum);

        log.info(CourseForumConstants.LOG_UPDATE_SUCCESS, existingForum.getForumName());
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "CourseForum", allEntries = true)
    public Boolean removeCourseForumById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_ID).is(id);
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseForum forum = mongoTemplate.findOne(query, CourseForum.class);
        if (forum == null) {
            throw new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS);
        }

        forum.setDeleted(DeletedEnum.DELETED.getCode());
        forum.setUpdateTime(LocalDateTime.now());
        courseForumRepository.save(forum);

        log.info(CourseForumConstants.LOG_DELETE_SUCCESS, forum.getForumName());
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "CourseForum", allEntries = true)
    public Integer removeCourseForumByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_LIST_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(CourseForumConstants.FIELD_ID).in(ids)
                .and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(CourseForumConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(CourseForumConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, CourseForum.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        log.info(CourseForumConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @CacheEvict(value = "CourseForum", allEntries = true)
    public Boolean updateForumStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }
        if (status == null || (status < CourseForumConstants.STATUS_MIN || status > CourseForumConstants.STATUS_MAX)) {
            throw new BusinessException(CourseForumEnum.FORUM_STATUS_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_ID).is(id);
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseForum forum = mongoTemplate.findOne(query, CourseForum.class);
        if (forum == null) {
            throw new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS);
        }

        forum.setStatus(status);
        forum.setUpdateTime(LocalDateTime.now());
        courseForumRepository.save(forum);

        log.info(CourseForumConstants.LOG_UPDATE_STATUS_SUCCESS, forum.getForumName(), status);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseForum", key = "'statistics:' + #p0", condition = "#p0 != null")
    public CourseForumVO getForumStatistics(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseForumConstants.FIELD_ID).is(id);
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseForum forum = mongoTemplate.findOne(query, CourseForum.class);
        if (forum == null) {
            throw new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS);
        }

        return convertToVO(forum);
    }

    /**
     * 将PO转换为VO
     *
     * @param forum 论坛PO
     * @return 论坛VO
     */
    private CourseForumVO convertToVO(CourseForum forum) {
        if (forum == null) {
            return null;
        }

        CourseForumVO vo = new CourseForumVO();
        BeanUtils.copyProperties(forum, vo);
        return vo;
    }
}
