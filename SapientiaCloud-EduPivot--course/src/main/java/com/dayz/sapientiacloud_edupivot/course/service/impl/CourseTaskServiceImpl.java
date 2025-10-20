package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.clients.TeacherClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseTaskConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseTask;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseTaskVO;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseTaskEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.TaskStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.CourseTaskRepository;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseTaskService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseTaskServiceImpl implements ICourseTaskService {

    private final CourseTaskRepository courseTaskRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;
    private final TeacherClient teacherClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseTaskVO> listCourseTask(CourseTaskQueryDTO courseTaskQueryDTO) {
        if (courseTaskQueryDTO == null) {
            throw new BusinessException(CourseTaskEnum.TASK_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (courseTaskQueryDTO.getCourseId() != null) {
            criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseTaskQueryDTO.getCourseId());
        }

        if (courseTaskQueryDTO.getSysUserId() != null) {
            criteria.and(CourseTaskConstants.FIELD_SYS_USER_ID).is(courseTaskQueryDTO.getSysUserId());
        }

        if (StringUtils.hasText(courseTaskQueryDTO.getTaskName())) {
            criteria.and(CourseTaskConstants.FIELD_TASK_NAME).regex(courseTaskQueryDTO.getTaskName(), CourseTaskConstants.REGEX_CASE_INSENSITIVE);
        }

        if (courseTaskQueryDTO.getTaskType() != null) {
            criteria.and(CourseTaskConstants.FIELD_TASK_TYPE).is(courseTaskQueryDTO.getTaskType());
        }

        if (courseTaskQueryDTO.getDifficulty() != null) {
            criteria.and(CourseTaskConstants.FIELD_DIFFICULTY).is(courseTaskQueryDTO.getDifficulty());
        }

        if (courseTaskQueryDTO.getStatus() != null) {
            criteria.and(CourseTaskConstants.FIELD_STATUS).is(courseTaskQueryDTO.getStatus());
        }

        if (courseTaskQueryDTO.getAutoGrade() != null) {
            criteria.and(CourseTaskConstants.FIELD_AUTO_GRADE).is(courseTaskQueryDTO.getAutoGrade());
        }

        if (courseTaskQueryDTO.getAllowLateSubmit() != null) {
            criteria.and(CourseTaskConstants.FIELD_ALLOW_LATE_SUBMIT).is(courseTaskQueryDTO.getAllowLateSubmit());
        }

        if (StringUtils.hasText(courseTaskQueryDTO.getTag())) {
            criteria.and(CourseTaskConstants.FIELD_TAGS).regex(courseTaskQueryDTO.getTag(), CourseTaskConstants.REGEX_CASE_INSENSITIVE);
        }

        if (courseTaskQueryDTO.getMinScore() != null) {
            criteria.and(CourseTaskConstants.FIELD_MAX_SCORE).gte(courseTaskQueryDTO.getMinScore());
        }
        if (courseTaskQueryDTO.getMaxScore() != null) {
            criteria.and(CourseTaskConstants.FIELD_MAX_SCORE).lte(courseTaskQueryDTO.getMaxScore());
        }

        if (courseTaskQueryDTO.getMinEstimatedTime() != null) {
            criteria.and(CourseTaskConstants.FIELD_ESTIMATED_TIME).gte(courseTaskQueryDTO.getMinEstimatedTime());
        }
        if (courseTaskQueryDTO.getMaxEstimatedTime() != null) {
            criteria.and(CourseTaskConstants.FIELD_ESTIMATED_TIME).lte(courseTaskQueryDTO.getMaxEstimatedTime());
        }

        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                courseTaskQueryDTO.getPageNum() - CourseTaskConstants.PAGE_NUM_OFFSET,
                courseTaskQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        long total = mongoTemplate.count(countQuery, CourseTask.class);

        List<CourseTaskVO> taskVOList = convertToVOList(tasks);

        PageInfo<CourseTaskVO> pageInfo = new PageInfo<>(taskVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(courseTaskQueryDTO.getPageNum());
        pageInfo.setPageSize(courseTaskQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / courseTaskQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'course:' + #p0", condition = "#p0 != null")
    public List<CourseTaskVO> listAllCourseTaskByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'user:' + #p0", condition = "#p0 != null")
    public List<CourseTaskVO> listAllCourseTaskByUserId(UUID sysUserId) {
        if (sysUserId == null) {
            throw new BusinessException(CourseTaskEnum.USER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_SYS_USER_ID).is(sysUserId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'course:' + #p0 + ':status:' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> listCourseTaskByCourseIdAndStatus(UUID courseId, Integer status) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }
        if (status == null || (status < CourseTaskConstants.STATUS_MIN || status > CourseTaskConstants.STATUS_MAX)) {
            throw new BusinessException(CourseTaskEnum.TASK_STATUS_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_STATUS).is(status);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'course:' + #p0 + ':type:' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> listCourseTaskByCourseIdAndTaskType(UUID courseId, Integer taskType) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }
        if (taskType == null || (taskType < CourseTaskConstants.TASK_TYPE_MIN || taskType > CourseTaskConstants.TASK_TYPE_MAX)) {
            throw new BusinessException(CourseTaskEnum.TASK_TYPE_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_TASK_TYPE).is(taskType);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'course:' + #p0 + ':difficulty:' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> listCourseTaskByCourseIdAndDifficulty(UUID courseId, Integer difficulty) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }
        if (difficulty == null || (difficulty < CourseTaskConstants.DIFFICULTY_MIN || difficulty > CourseTaskConstants.DIFFICULTY_MAX)) {
            throw new BusinessException(CourseTaskEnum.TASK_STATUS_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_DIFFICULTY).is(difficulty);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "#p0", condition = "#p0 != null")
    public CourseTaskVO getCourseTaskById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        return convertToVO(task);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "CourseTask", key = "'user:' + #p0.sysUserId")
    })
    public CourseTaskVO addCourseTask(CourseTaskDTO courseTaskDTO) {
        if (courseTaskDTO == null) {
            throw new BusinessException(CourseTaskEnum.TASK_REQUIRED);
        }

        if (courseTaskDTO.getCourseId() == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }

        if (courseTaskDTO.getSysUserId() == null) {
            throw new BusinessException(CourseTaskEnum.USER_ID_REQUIRED);
        }

        if (!StringUtils.hasText(courseTaskDTO.getTaskName())) {
            throw new BusinessException(CourseTaskEnum.TASK_NAME_REQUIRED);
        }

        if (!StringUtils.hasText(courseTaskDTO.getTaskContent())) {
            throw new BusinessException(CourseTaskEnum.TASK_CONTENT_REQUIRED);
        }

        if (courseTaskDTO.getTaskType() == null ||
                courseTaskDTO.getTaskType() < CourseTaskConstants.TASK_TYPE_MIN ||
                courseTaskDTO.getTaskType() > CourseTaskConstants.TASK_TYPE_MAX) {
            throw new BusinessException(CourseTaskEnum.TASK_TYPE_INVALID);
        }

        if (courseTaskDTO.getMaxScore() == null || courseTaskDTO.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(CourseTaskEnum.MAX_SCORE_INVALID);
        }

        if (courseTaskDTO.getStartTime() == null) {
            throw new BusinessException(CourseTaskEnum.START_TIME_REQUIRED);
        }
        if (courseTaskDTO.getEndTime() == null) {
            throw new BusinessException(CourseTaskEnum.END_TIME_REQUIRED);
        }

        CourseTask task = new CourseTask();
        BeanUtils.copyProperties(courseTaskDTO, task);

        task.setId(UuidCreator.getTimeOrderedEpoch());

        if (task.getStatus() == null) {
            task.setStatus(CourseTaskConstants.DEFAULT_STATUS);
        }
        if (task.getViewCount() == null) {
            task.setViewCount(CourseTaskConstants.DEFAULT_VIEW_COUNT);
        }
        if (task.getAllowLateSubmit() == null) {
            task.setAllowLateSubmit(CourseTaskConstants.DEFAULT_ALLOW_LATE_SUBMIT);
        }
        if (task.getMaxSubmitCount() == null) {
            task.setMaxSubmitCount(CourseTaskConstants.DEFAULT_MAX_SUBMIT_COUNT);
        }
        if (task.getAutoGrade() == null) {
            task.setAutoGrade(CourseTaskConstants.DEFAULT_AUTO_GRADE);
        }
        if (task.getDifficulty() == null) {
            task.setDifficulty(CourseTaskConstants.DEFAULT_DIFFICULTY);
        }
        if (task.getEstimatedTime() == null) {
            task.setEstimatedTime(CourseTaskConstants.DEFAULT_ESTIMATED_TIME);
        }
        if (task.getDeleted() == null) {
            task.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        LocalDateTime now = LocalDateTime.now();
        task.setCreateTime(now);
        task.setUpdateTime(now);

        CourseTask savedTask = courseTaskRepository.save(task);

        return convertToVO(savedTask);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0.id"),
            @CacheEvict(value = "CourseTask", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "CourseTask", key = "'user:' + #p0.sysUserId")
    })
    public Boolean updateCourseTask(CourseTaskDTO courseTaskDTO) {
        if (courseTaskDTO == null || courseTaskDTO.getId() == null) {
            throw new BusinessException(CourseTaskEnum.TASK_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(courseTaskDTO.getId());
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask existingTask = mongoTemplate.findOne(query, CourseTask.class);
        if (existingTask == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        if (StringUtils.hasText(courseTaskDTO.getTaskName())) {
            existingTask.setTaskName(courseTaskDTO.getTaskName());
        }
        if (StringUtils.hasText(courseTaskDTO.getDescription())) {
            existingTask.setDescription(courseTaskDTO.getDescription());
        }
        if (StringUtils.hasText(courseTaskDTO.getTaskContent())) {
            existingTask.setTaskContent(courseTaskDTO.getTaskContent());
        }
        if (courseTaskDTO.getTaskType() != null) {
            existingTask.setTaskType(courseTaskDTO.getTaskType());
        }
        if (courseTaskDTO.getMaxScore() != null) {
            existingTask.setMaxScore(courseTaskDTO.getMaxScore());
        }
        if (courseTaskDTO.getStartTime() != null) {
            existingTask.setStartTime(courseTaskDTO.getStartTime());
        }
        if (courseTaskDTO.getEndTime() != null) {
            existingTask.setEndTime(courseTaskDTO.getEndTime());
        }
        if (courseTaskDTO.getAllowLateSubmit() != null) {
            existingTask.setAllowLateSubmit(courseTaskDTO.getAllowLateSubmit());
        }
        if (courseTaskDTO.getMaxSubmitCount() != null) {
            existingTask.setMaxSubmitCount(courseTaskDTO.getMaxSubmitCount());
        }
        if (courseTaskDTO.getAutoGrade() != null) {
            existingTask.setAutoGrade(courseTaskDTO.getAutoGrade());
        }
        if (courseTaskDTO.getTags() != null) {
            existingTask.setTags(courseTaskDTO.getTags());
        }
        if (courseTaskDTO.getDifficulty() != null) {
            existingTask.setDifficulty(courseTaskDTO.getDifficulty());
        }
        if (courseTaskDTO.getEstimatedTime() != null) {
            existingTask.setEstimatedTime(courseTaskDTO.getEstimatedTime());
        }
        if (courseTaskDTO.getAttachmentUrls() != null) {
            existingTask.setAttachmentUrls(courseTaskDTO.getAttachmentUrls());
        }
        if (courseTaskDTO.getResourceUrls() != null) {
            existingTask.setResourceUrls(courseTaskDTO.getResourceUrls());
        }
        if (courseTaskDTO.getStatus() != null) {
            existingTask.setStatus(courseTaskDTO.getStatus());
        }

        existingTask.setUpdateTime(LocalDateTime.now());

        courseTaskRepository.save(existingTask);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean removeCourseTaskById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setDeleted(DeletedEnum.DELETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Integer removeCourseTaskByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_LIST_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(CourseTaskConstants.FIELD_ID).in(ids)
                .and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(CourseTaskConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(CourseTaskConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, CourseTask.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean updateTaskStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }
        if (status == null || (status < CourseTaskConstants.STATUS_MIN || status > CourseTaskConstants.STATUS_MAX)) {
            throw new BusinessException(CourseTaskEnum.TASK_STATUS_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setStatus(status);
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean publishTask(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setStatus(TaskStatusEnum.PUBLISHED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean unpublishTask(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setStatus(TaskStatusEnum.DRAFT.getCode());
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean startTask(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setStatus(TaskStatusEnum.IN_PROGRESS.getCode());
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0"),
            @CacheEvict(value = "CourseTask", allEntries = true)
    })
    public Boolean endTask(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_ID).is(id);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseTask task = mongoTemplate.findOne(query, CourseTask.class);
        if (task == null) {
            throw new BusinessException(CourseTaskEnum.TASK_NOT_EXISTS);
        }

        task.setStatus(TaskStatusEnum.ENDED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        courseTaskRepository.save(task);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseTask", key = "#p0")
    })
    public Boolean viewTask(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseTaskEnum.TASK_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(CourseTaskConstants.FIELD_ID).is(id));
        Update update = new Update().inc(CourseTaskConstants.FIELD_VIEW_COUNT, CourseTaskConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, CourseTask.class);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'hot:' + #p0 + ':' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> getHotTasks(UUID courseId, Integer limit) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_VIEW_COUNT, CourseTaskConstants.FIELD_CREATE_TIME));
        query.limit(limit);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'latest:' + #p0 + ':' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> getLatestTasks(UUID courseId, Integer limit) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));
        query.limit(limit);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'tag:' + #p0 + ':' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> getTasksByTag(String tag, Integer limit) {
        if (!StringUtils.hasText(tag)) {
            throw new BusinessException(CourseTaskEnum.TASK_REQUIRED);
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_TAGS).regex(tag, CourseTaskConstants.REGEX_CASE_INSENSITIVE);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));
        query.limit(limit);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'search:' + #p0 + ':' + #p1", condition = "#p0 != null && #p1 != null")
    public List<CourseTaskVO> searchTasksByName(String taskName, Integer limit) {
        if (!StringUtils.hasText(taskName)) {
            throw new BusinessException(CourseTaskEnum.TASK_NAME_REQUIRED);
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_TASK_NAME).regex(taskName, CourseTaskConstants.REGEX_CASE_INSENSITIVE);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseTaskConstants.FIELD_CREATE_TIME));
        query.limit(limit);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        return convertToVOList(tasks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'statistics:' + #p0", condition = "#p0 != null")
    public Map<String, Object> getTaskStatistics(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseTaskEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        long totalCount = mongoTemplate.count(query, CourseTask.class);

        Query draftQuery = new Query();
        draftQuery.addCriteria(criteria);
        draftQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.DRAFT.getCode()));
        long draftCount = mongoTemplate.count(draftQuery, CourseTask.class);

        Query publishedQuery = new Query();
        publishedQuery.addCriteria(criteria);
        publishedQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.PUBLISHED.getCode()));
        long publishedCount = mongoTemplate.count(publishedQuery, CourseTask.class);

        Query runningQuery = new Query();
        runningQuery.addCriteria(criteria);
        runningQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.IN_PROGRESS.getCode()));
        long runningCount = mongoTemplate.count(runningQuery, CourseTask.class);

        Query endedQuery = new Query();
        endedQuery.addCriteria(criteria);
        endedQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.ENDED.getCode()));
        long endedCount = mongoTemplate.count(endedQuery, CourseTask.class);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        long totalViews = tasks.stream()
                .mapToLong(task -> task.getViewCount() != null ? task.getViewCount() : 0L)
                .sum();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put(CourseTaskConstants.STATS_TOTAL_COUNT, totalCount);
        statistics.put(CourseTaskConstants.STATS_DRAFT_COUNT, draftCount);
        statistics.put(CourseTaskConstants.STATS_PUBLISHED_COUNT, publishedCount);
        statistics.put(CourseTaskConstants.STATS_RUNNING_COUNT, runningCount);
        statistics.put(CourseTaskConstants.STATS_ENDED_COUNT, endedCount);
        statistics.put(CourseTaskConstants.STATS_TOTAL_VIEWS, totalViews);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTask", key = "'userStats:' + #p0", condition = "#p0 != null")
    public Map<String, Object> getUserTaskStatistics(UUID sysUserId) {
        if (sysUserId == null) {
            throw new BusinessException(CourseTaskEnum.USER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseTaskConstants.FIELD_SYS_USER_ID).is(sysUserId);
        criteria.and(CourseTaskConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        long totalCount = mongoTemplate.count(query, CourseTask.class);

        Query draftQuery = new Query();
        draftQuery.addCriteria(criteria);
        draftQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.DRAFT.getCode()));
        long draftCount = mongoTemplate.count(draftQuery, CourseTask.class);

        Query publishedQuery = new Query();
        publishedQuery.addCriteria(criteria);
        publishedQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.PUBLISHED.getCode()));
        long publishedCount = mongoTemplate.count(publishedQuery, CourseTask.class);

        Query runningQuery = new Query();
        runningQuery.addCriteria(criteria);
        runningQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.IN_PROGRESS.getCode()));
        long runningCount = mongoTemplate.count(runningQuery, CourseTask.class);

        Query endedQuery = new Query();
        endedQuery.addCriteria(criteria);
        endedQuery.addCriteria(Criteria.where(CourseTaskConstants.FIELD_STATUS).is(TaskStatusEnum.ENDED.getCode()));
        long endedCount = mongoTemplate.count(endedQuery, CourseTask.class);

        List<CourseTask> tasks = mongoTemplate.find(query, CourseTask.class);
        long totalViews = tasks.stream()
                .mapToLong(task -> task.getViewCount() != null ? task.getViewCount() : 0L)
                .sum();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put(CourseTaskConstants.STATS_TOTAL_COUNT, totalCount);
        statistics.put(CourseTaskConstants.STATS_DRAFT_COUNT, draftCount);
        statistics.put(CourseTaskConstants.STATS_PUBLISHED_COUNT, publishedCount);
        statistics.put(CourseTaskConstants.STATS_RUNNING_COUNT, runningCount);
        statistics.put(CourseTaskConstants.STATS_ENDED_COUNT, endedCount);
        statistics.put(CourseTaskConstants.STATS_TOTAL_VIEWS, totalViews);

        return statistics;
    }

    private List<CourseTaskVO> convertToVOList(List<CourseTask> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return new ArrayList<>();
        }

        Map<UUID, SysUserVO> userMap = getUserMap();
        Map<UUID, TeacherVO> teacherMap = getTeacherMap();

        return tasks.stream()
                .map(task -> convertToVOWithMaps(task, userMap, teacherMap))
                .toList();
    }

    private CourseTaskVO convertToVO(CourseTask task) {
        if (task == null) {
            return null;
        }

        Map<UUID, SysUserVO> userMap = getUserMap();
        Map<UUID, TeacherVO> teacherMap = getTeacherMap();

        return convertToVOWithMaps(task, userMap, teacherMap);
    }

    private CourseTaskVO convertToVOWithMaps(CourseTask task, Map<UUID, SysUserVO> userMap, Map<UUID, TeacherVO> teacherMap) {
        if (task == null) {
            return null;
        }

        CourseTaskVO vo = new CourseTaskVO();
        BeanUtils.copyProperties(task, vo);

        fillUserInfo(vo, task.getSysUserId(), userMap);

        fillTeacherInfo(vo, task.getSysUserId(), teacherMap);


        return vo;
    }

    private Map<UUID, SysUserVO> getUserMap() {
        Result<List<SysUserVO>> result = sysUserClient.listAllSysUser();
        if (result != null && result.getData() != null) {
            return result.getData().stream()
                    .collect(Collectors.toMap(SysUserVO::getId, user -> user));
        }
        return new HashMap<>();
    }

    private Map<UUID, TeacherVO> getTeacherMap() {
        Result<List<TeacherVO>> result = teacherClient.listAllTeacher();
        if (result != null && result.getData() != null) {
            return result.getData().stream()
                    .collect(Collectors.toMap(TeacherVO::getSysUserId, teacher -> teacher));
        }
        return new HashMap<>();
    }

    private void fillUserInfo(CourseTaskVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            // 设置用户基本信息
            vo.setTeacherName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            vo.setTeacherAvatar(user.getAvatar());
        }
    }

    private void fillTeacherInfo(CourseTaskVO vo, UUID sysUserId, Map<UUID, TeacherVO> teacherMap) {
        if (sysUserId == null || teacherMap.isEmpty()) {
            return;
        }

        TeacherVO teacher = teacherMap.get(sysUserId);
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
            vo.setTeacherAvatar(teacher.getAvatar());
        }
    }

}
