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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;

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

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 课程ID
        if (courseForumQueryDTO.getCourseId() != null) {
            criteria.and(CourseForumConstants.FIELD_COURSE_ID).is(courseForumQueryDTO.getCourseId());
        }

        // 论坛名称模糊查询
        if (StringUtils.hasText(courseForumQueryDTO.getForumName())) {
            criteria.and(CourseForumConstants.FIELD_FORUM_NAME).regex(courseForumQueryDTO.getForumName(), CourseForumConstants.REGEX_CASE_INSENSITIVE);
        }

        // 论坛类型
        if (courseForumQueryDTO.getForumType() != null) {
            criteria.and(CourseForumConstants.FIELD_FORUM_TYPE).is(courseForumQueryDTO.getForumType());
        }

        // 状态
        if (courseForumQueryDTO.getStatus() != null) {
            criteria.and(CourseForumConstants.FIELD_STATUS).is(courseForumQueryDTO.getStatus());
        }

        // 逻辑删除
        criteria.and(CourseForumConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, CourseForumConstants.FIELD_SORT_ORDER, CourseForumConstants.FIELD_FORUM_NAME));

        // 分页
        Pageable pageable = PageRequest.of(
                courseForumQueryDTO.getPageNum() - CourseForumConstants.PAGE_NUM_OFFSET,
                courseForumQueryDTO.getPageSize()
        );
        query.with(pageable);

        // 执行查询
        List<CourseForum> forums = mongoTemplate.find(query, CourseForum.class);
        long total = mongoTemplate.count(query, CourseForum.class);

        // 转换为VO
        List<CourseForumVO> forumVOList = forums.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页信息
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
    public List<CourseForumVO> listCourseForumByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseForumEnum.COURSE_ID_REQUIRED);
        }

        List<CourseForum> forums = courseForumRepository.findByCourseIdOrderBySortOrderAsc(courseId);
        return forums.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseForum", key = "#p0", condition = "#p0 != null")
    public CourseForumVO getCourseForumById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        CourseForum forum = courseForumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

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

        // 验证课程ID
        if (courseForumDTO.getCourseId() == null) {
            throw new BusinessException(CourseForumEnum.COURSE_ID_REQUIRED);
        }

        // 验证论坛名称
        if (!StringUtils.hasText(courseForumDTO.getForumName())) {
            throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
        }

        // 检查论坛名称是否重复
        if (courseForumRepository.existsByCourseIdAndForumName(
                courseForumDTO.getCourseId(), courseForumDTO.getForumName())) {
            throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
        }

        // 创建论坛实体
        CourseForum forum = new CourseForum();
        BeanUtils.copyProperties(courseForumDTO, forum);
        
        // 设置ID
        forum.setId(UuidCreator.getTimeOrderedEpoch());
        
        // 设置默认值
        if (forum.getStatus() == null) {
            forum.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (forum.getPostCount() == null) {
            forum.setPostCount(CourseForumConstants.DEFAULT_POST_COUNT);
        }
        if (forum.getReplyCount() == null) {
            forum.setReplyCount(CourseForumConstants.DEFAULT_REPLY_COUNT);
        }
        if (forum.getSortOrder() == null) {
            forum.setSortOrder(CourseForumConstants.DEFAULT_SORT_ORDER);
        }
        if (forum.getDeleted() == null) {
            forum.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        forum.setCreateTime(now);
        forum.setUpdateTime(now);

        // 保存论坛
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

        // 检查论坛是否存在
        CourseForum existingForum = courseForumRepository.findById(courseForumDTO.getId())
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

        // 验证论坛名称是否重复（排除自己）
        if (StringUtils.hasText(courseForumDTO.getForumName()) &&
                !courseForumDTO.getForumName().equals(existingForum.getForumName())) {
            if (courseForumRepository.existsByCourseIdAndForumName(
                    courseForumDTO.getCourseId(), courseForumDTO.getForumName())) {
                throw new BusinessException(CourseForumEnum.FORUM_NAME_EXISTS);
            }
        }

        // 更新字段
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
        if (courseForumDTO.getModeratorIds() != null) {
            existingForum.setModeratorIds(courseForumDTO.getModeratorIds());
        }
        if (courseForumDTO.getSortOrder() != null) {
            existingForum.setSortOrder(courseForumDTO.getSortOrder());
        }
        if (courseForumDTO.getStatus() != null) {
            existingForum.setStatus(courseForumDTO.getStatus());
        }
        if (courseForumDTO.getRules() != null) {
            existingForum.setRules(courseForumDTO.getRules());
        }
        if (courseForumDTO.getTags() != null) {
            existingForum.setTags(courseForumDTO.getTags());
        }

        // 更新时间
        existingForum.setUpdateTime(LocalDateTime.now());

        // 保存更新
        courseForumRepository.save(existingForum);
        
        log.info(CourseForumConstants.LOG_UPDATE_SUCCESS, existingForum.getForumName());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", key = "#p0"),
            @CacheEvict(value = "CourseForum", allEntries = true)
    })
    public Boolean removeCourseForumById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        // 检查论坛是否存在
        CourseForum forum = courseForumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

        // 逻辑删除
        forum.setDeleted(DeletedEnum.DELETED.getCode());
        forum.setUpdateTime(LocalDateTime.now());
        courseForumRepository.save(forum);
        
        log.info(CourseForumConstants.LOG_DELETE_SUCCESS, forum.getForumName());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", allEntries = true)
    })
    public Integer removeCourseForumByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_LIST_REQUIRED);
        }

        int deletedCount = 0;
        for (UUID id : ids) {
            try {
                if (removeCourseForumById(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn(CourseForumConstants.LOG_DELETE_FAILED, id, e.getMessage());
            }
        }

        log.info(CourseForumConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", key = "#p0"),
            @CacheEvict(value = "CourseForum", allEntries = true)
    })
    public Boolean updateForumStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }
        if (status == null || (status < CourseForumConstants.STATUS_MIN || status > CourseForumConstants.STATUS_MAX)) {
            throw new BusinessException(CourseForumEnum.FORUM_STATUS_INVALID);
        }

        // 检查论坛是否存在
        CourseForum forum = courseForumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

        // 更新状态
        forum.setStatus(status);
        forum.setUpdateTime(LocalDateTime.now());
        courseForumRepository.save(forum);
        
        log.info(CourseForumConstants.LOG_UPDATE_STATUS_SUCCESS, forum.getForumName(), status);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseForum", key = "#p0"),
            @CacheEvict(value = "CourseForum", allEntries = true)
    })
    public Boolean setForumModerators(UUID id, List<UUID> moderatorIds) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        // 检查论坛是否存在
        CourseForum forum = courseForumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

        // 设置版主
        forum.setModeratorIds(moderatorIds);
        forum.setUpdateTime(LocalDateTime.now());
        courseForumRepository.save(forum);
        
        log.info(CourseForumConstants.LOG_SET_MODERATORS_SUCCESS, forum.getForumName());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseForum", key = "'statistics:' + #p0", condition = "#p0 != null")
    public CourseForumVO getForumStatistics(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseForumEnum.FORUM_ID_REQUIRED);
        }

        CourseForum forum = courseForumRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CourseForumEnum.FORUM_NOT_EXISTS));

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
