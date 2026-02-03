package com.dayz.sapientiacloud_edupivot.classroom.service.impl;

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

    /**
     * 根据开始时间和结束时间实时计算课程状态
     * 0=课前准备, 1=上课中, 2=下课
     */
    private Integer calculateStatus(LocalDateTime startTime, LocalDateTime overTime) {
        LocalDateTime now = LocalDateTime.now();

        // 没有开始时间，默认为课前准备
        if (startTime == null) {
            return 0;
        }

        // 当前时间 < 开始时间 → 课前准备
        if (now.isBefore(startTime)) {
            return 0;
        }

        // 没有结束时间，且当前时间 >= 开始时间 → 上课中
        if (overTime == null) {
            return 1;
        }

        // 开始时间 <= 当前时间 <= 结束时间 → 上课中
        if (!now.isBefore(startTime) && !now.isAfter(overTime)) {
            return 1;
        }

        // 当前时间 > 结束时间 → 下课
        return 2;
    }

    /**
     * 为VO列表中的每条记录实时计算状态
     */
    private void calculateStatusForList(List<CourseRecordVO> list) {
        if (list != null) {
            list.forEach(record -> record.setStatus(calculateStatus(record.getStartTime(), record.getOverTime())));
        }
    }

    @Override
    public PageInfo<CourseRecordVO> listCourseRecordPage(CourseRecordQueryDTO courseRecordQueryDTO) {
        if (courseRecordQueryDTO == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_REQUIRED);
        }

        PageInfo<CourseRecordVO> pageInfo = PageHelper.startPage(courseRecordQueryDTO.getPageNum(), courseRecordQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseRecordMapper.listCourseRecord(courseRecordQueryDTO));

        // 实时计算每条记录的状态
        calculateStatusForList(pageInfo.getList());

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseRecord", key = "'all'", condition = "true")
    public List<CourseRecordVO> listAllCourseRecord() {
        List<CourseRecordVO> list = courseRecordMapper.listAllCourseRecord();
        // 实时计算每条记录的状态
        calculateStatusForList(list);
        return list;
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

        // 实时计算状态
        courseRecordVO.setStatus(calculateStatus(courseRecordVO.getStartTime(), courseRecordVO.getOverTime()));

        return courseRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"CourseRecord", "CourseRecordStudent"}, allEntries = true)
    public CourseRecordVO addCourseRecord(CourseRecordDTO courseRecordDTO) {
        if (courseRecordDTO == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_INFO_REQUIRED);
        }

        // 验证必填字段
        if (courseRecordDTO.getCourseId() == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_ID_REQUIRED);
        }
        if (courseRecordDTO.getTeacherId() == null) {
            throw new BusinessException(CourseRecordEnum.TEACHER_ID_REQUIRED);
        }
        if (courseRecordDTO.getClassroomType() == null) {
            throw new BusinessException(CourseRecordEnum.CLASSROOM_TYPE_REQUIRED);
        }

        // 创建课程记录
        CourseRecord courseRecord = new CourseRecord();
        BeanUtils.copyProperties(courseRecordDTO, courseRecord);

        courseRecord.setId(UuidCreator.getTimeOrderedEpoch());
        courseRecord.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        courseRecord.setCreateTime(LocalDateTime.now());
        courseRecord.setUpdateTime(LocalDateTime.now());

        this.save(courseRecord);

        // 返回结果
        CourseRecordVO courseRecordVO = new CourseRecordVO();
        BeanUtils.copyProperties(courseRecord, courseRecordVO);
        // 实时计算状态
        courseRecordVO.setStatus(calculateStatus(courseRecordVO.getStartTime(), courseRecordVO.getOverTime()));
        return courseRecordVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecord", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "CourseRecord", key = "'all'", condition = "true"),
            @CacheEvict(value = "CourseRecordStudent", allEntries = true)
    })
    public Boolean updateCourseRecord(CourseRecordDTO courseRecordDTO) {
        if (courseRecordDTO == null || courseRecordDTO.getId() == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_INFO_OR_ID_REQUIRED);
        }

        // 检查记录是否存在
        CourseRecord existingRecord = this.getById(courseRecordDTO.getId());
        if (existingRecord == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_EXISTS);
        }

        // 更新记录
        CourseRecord courseRecord = new CourseRecord();
        BeanUtils.copyProperties(courseRecordDTO, courseRecord);
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

        // 检查状态：上课中的课程不允许删除
        if (calculateStatus(courseRecord.getStartTime(), courseRecord.getOverTime()) == 1) {
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

        List<CourseRecordVO> list = courseRecordMapper.listCourseRecordByCourseId(courseId);
        // 实时计算每条记录的状态
        calculateStatusForList(list);
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRecordVO> listCourseRecordByTeacherId(UUID teacherId) {
        if (teacherId == null) {
            throw new BusinessException(CourseRecordEnum.TEACHER_ID_REQUIRED);
        }

        List<CourseRecordVO> list = courseRecordMapper.listCourseRecordByTeacherId(teacherId);
        // 实时计算每条记录的状态
        calculateStatusForList(list);
        return list;
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

        // 检查状态：只有上课中的课程才能提前结束
        if (calculateStatus(courseRecord.getStartTime(), courseRecord.getOverTime()) != 1) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_NOT_STARTED);
        }

        // 更新结束时间为当前时间
        courseRecord.setOverTime(LocalDateTime.now());
        courseRecord.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDateTime getCourseEndTimeById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseRecordEnum.COURSE_RECORD_ID_REQUIRED);
        }
        return courseRecordMapper.getOverTimeById(id);
    }
}
