package com.dayz.sapientiacloud_edupivot.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.classroom.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.classroom.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.CourseRecord;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.dayz.sapientiacloud_edupivot.classroom.enums.CourseRecordEnum;
import com.dayz.sapientiacloud_edupivot.classroom.mapper.CourseRecordMapper;
import com.dayz.sapientiacloud_edupivot.classroom.mapper.CourseRecordStudentMapper;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRecordServiceImpl extends ServiceImpl<CourseRecordMapper, CourseRecord> implements ICourseRecordService {

    private final CourseRecordMapper courseRecordMapper;
    private final CourseRecordStudentMapper courseRecordStudentMapper;

    @Override
    public PageInfo<CourseRecordVO> listCourseRecordPage(CourseRecordQueryDTO dto) {
        if (dto == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_REQUIRED);
        }

        return PageHelper.startPage(dto.getPageNum(), dto.getPageSize())
                .doSelectPageInfo(() -> courseRecordMapper.listCourseRecord(dto));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseRecord", key = "'all'", condition = "true")
    public List<CourseRecordVO> listAllCourseRecord() {
        return courseRecordMapper.listAllCourseRecord();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseRecord", key = "#p0", condition = "#p0 != null")
    public CourseRecordVO getCourseRecordById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ID_REQUIRED);
        }

        CourseRecordVO courseRecordVO = courseRecordMapper.getCourseRecordById(id);
        if (courseRecordVO == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_EXISTS);
        }

        return courseRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"CourseRecord", "CourseRecordStudent"}, allEntries = true)
    public CourseRecordVO addCourseRecord(CourseRecordDTO dto) {
        if (dto == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_INFO_REQUIRED);
        }

        // 验证必填字段
        if (dto.getCourseId() == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_ID_REQUIRED);
        }
        if (dto.getTeacherId() == null) {
            throw new BusinessException(CourseRecordEnum.TEACHER_ID_REQUIRED);
        }
        if (dto.getModelType() == null || dto.getModelType().isBlank()) {
            throw new BusinessException(CourseRecordEnum.MODEL_TYPE_REQUIRED);
        }
        if (dto.getTotalDesks() == null) {
            throw new BusinessException(CourseRecordEnum.TOTAL_DESKS_REQUIRED);
        }

        // 创建课程记录
        CourseRecord courseRecord = new CourseRecord();
        BeanUtils.copyProperties(dto, courseRecord);

        courseRecord.setId(UuidCreator.getTimeOrderedEpoch());
        courseRecord.setStatus(0); // 未开始
        courseRecord.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        courseRecord.setCreateTime(LocalDateTime.now());
        courseRecord.setUpdateTime(LocalDateTime.now());

        this.save(courseRecord);

        // 返回结果
        CourseRecordVO courseRecordVO = new CourseRecordVO();
        BeanUtils.copyProperties(courseRecord, courseRecordVO);
        return courseRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecord", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "CourseRecord", key = "'all'", condition = "true"),
            @CacheEvict(value = "CourseRecordStudent", allEntries = true)
    })
    public Boolean updateCourseRecord(CourseRecordDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_INFO_OR_ID_REQUIRED);
        }

        // 检查记录是否存在
        CourseRecord existingRecord = this.getById(dto.getId());
        if (existingRecord == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_EXISTS);
        }

        // 检查状态：已结束的课程不允许修改
        if (existingRecord.getStatus() != null && existingRecord.getStatus() == 2) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ALREADY_ENDED);
        }

        // 更新记录
        CourseRecord courseRecord = new CourseRecord();
        BeanUtils.copyProperties(dto, courseRecord);
        courseRecord.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecord", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "CourseRecord", key = "'all'", condition = "true"),
            @CacheEvict(value = "CourseRecordStudent", allEntries = true)
    })
    public Boolean removeCourseRecordById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ID_REQUIRED);
        }

        // 检查记录是否存在
        CourseRecord courseRecord = this.getById(id);
        if (courseRecord == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_EXISTS);
        }

        // 检查是否有学生座位记录
        Integer studentCount = courseRecordStudentMapper.countStudentsByRecordId(id);
        if (studentCount != null && studentCount > 0) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_HAS_STUDENTS);
        }

        // 检查状态：进行中的课程不允许删除
        if (courseRecord.getStatus() != null && courseRecord.getStatus() == 1) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_IN_PROGRESS);
        }

        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"CourseRecord", "CourseRecordStudent"}, allEntries = true)
    public Integer removeCourseRecordByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ID_LIST_REQUIRED);
        }

        // 逐个检查并删除
        int count = 0;
        StringBuilder errorMsg = new StringBuilder();
        for (UUID id : ids) {
            try {
                if (removeCourseRecordById(id)) {
                    count++;
                }
            } catch (BusinessException e) {
                String msg = String.format("id=%s: %s", id, e.getMessage());
                errorMsg.append(msg).append("; ");
                log.warn("删除课程记录失败: {}", msg);
            }
        }
        
        // 如果全部删除失败，抛出异常，确保前后端响应一致
        if (count == 0 && !ids.isEmpty()) {
            throw new BusinessException("批量删除失败: " + errorMsg.toString());
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRecordVO> listCourseRecordByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_ID_REQUIRED);
        }

        return courseRecordMapper.listCourseRecordByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRecordVO> listCourseRecordByTeacherId(UUID teacherId) {
        if (teacherId == null) {
            throw new BusinessException(CourseRecordEnum.TEACHER_ID_REQUIRED);
        }

        return courseRecordMapper.listCourseRecordByTeacherId(teacherId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecord", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "CourseRecord", key = "'all'", condition = "true")
    })
    public Boolean endCourseRecord(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ID_REQUIRED);
        }

        // 检查记录是否存在
        CourseRecord courseRecord = this.getById(id);
        if (courseRecord == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_EXISTS);
        }

        // 检查状态：只有进行中的课程才能结束
        if (courseRecord.getStatus() == null || courseRecord.getStatus() != 1) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_STARTED);
        }

        // 更新状态为已结束
        courseRecord.setStatus(2);
        courseRecord.setOverTime(LocalDateTime.now());
        courseRecord.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseRecord);
    }
}
