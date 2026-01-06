package com.dayz.sapientiacloud_edupivot.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.classroom.common.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.classroom.common.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.classroom.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.StudentSeatDeleteDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.CourseRecordStudent;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.dayz.sapientiacloud_edupivot.classroom.enums.CourseRecordStudentEnum;
import com.dayz.sapientiacloud_edupivot.classroom.mapper.CourseRecordStudentMapper;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordService;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordStudentService;
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
public class CourseRecordStudentServiceImpl extends ServiceImpl<CourseRecordStudentMapper, CourseRecordStudent> implements ICourseRecordStudentService {

    private final CourseRecordStudentMapper courseRecordStudentMapper;
    private final ICourseRecordService courseRecordService;
    private final StudentClient studentClient;

    @Override
    public PageInfo<CourseRecordStudentVO> listCourseRecordStudentPage(CourseRecordStudentQueryDTO courseRecordStudentQueryDTO) {
        if (courseRecordStudentQueryDTO == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_STUDENT_REQUIRED);
        }

        return PageHelper.startPage(courseRecordStudentQueryDTO.getPageNum(), courseRecordStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseRecordStudentMapper.listCourseRecordStudent(courseRecordStudentQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseRecordStudent", key = "'all'", condition = "true")
    public List<CourseRecordStudentVO> listAllCourseRecordStudent() {
        return courseRecordStudentMapper.listAllCourseRecordStudent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseRecordStudentVO> listStudentsByRecordId(UUID recordId) {
        if (recordId == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_REQUIRED);
        }

        return courseRecordStudentMapper.listStudentsByRecordId(recordId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseRecordStudent", key = "#p0 + ':' + #p1", condition = "#p0 != null and #p1 != null")
    public CourseRecordStudentVO getStudentSeat(UUID recordId, UUID studentId) {
        if (recordId == null || studentId == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_AND_STUDENT_ID_REQUIRED);
        }

        CourseRecordStudentVO studentSeat = courseRecordStudentMapper.getStudentSeat(recordId, studentId);
        if (studentSeat == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_STUDENT_NOT_EXISTS);
        }

        return studentSeat;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "CourseRecordStudent", allEntries = true)
    public CourseRecordStudentVO addStudentSeat(CourseRecordStudentDTO courseRecordStudentDTO) {
        if (courseRecordStudentDTO == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_STUDENT_INFO_REQUIRED);
        }

        // 验证必填字段
        if (courseRecordStudentDTO.getRecordId() == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_REQUIRED);
        }
        if (courseRecordStudentDTO.getStudentId() == null) {
            throw new BusinessException(CourseRecordStudentEnum.STUDENT_ID_REQUIRED);
        }
        if (courseRecordStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_ID_REQUIRED);
        }
        if (courseRecordStudentDTO.getSeatIndex() == null) {
            throw new BusinessException(CourseRecordStudentEnum.SEAT_INDEX_REQUIRED);
        }
        if (courseRecordStudentDTO.getLocationX() == null || courseRecordStudentDTO.getLocationY() == null) {
            throw new BusinessException(CourseRecordStudentEnum.LOCATION_REQUIRED);
        }

        // 检查学生是否存在
        Result<StudentVO> studentResult = studentClient.getStudentById(courseRecordStudentDTO.getStudentId());
        if (studentResult == null || !studentResult.isSuccess() || studentResult.getData() == null) {
            throw new BusinessException(CourseRecordStudentEnum.STUDENT_NOT_EXISTS);
        }

        // 检查课程记录是否存在
        CourseRecordVO courseRecordVO = courseRecordService.getCourseRecordById(courseRecordStudentDTO.getRecordId());
        if (courseRecordVO == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_NOT_EXISTS);
        }

        // 检查课程状态
        if (courseRecordVO.getStatus() == null || courseRecordVO.getStatus() == 0) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_NOT_STARTED);
        }
        if (courseRecordVO.getStatus() == 2) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_ENDED);
        }
        if (courseRecordVO.getStatus() == 3) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_CANCELLED);
        }

        // 检查学生是否已经选座
        LambdaQueryWrapper<CourseRecordStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRecordStudent::getRecordId, courseRecordStudentDTO.getRecordId())
                .eq(CourseRecordStudent::getStudentId, courseRecordStudentDTO.getStudentId());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseRecordStudentEnum.STUDENT_ALREADY_SEATED);
        }

        // 检查座位是否被占用
        Integer occupiedCount = courseRecordStudentMapper.checkSeatOccupied(courseRecordStudentDTO.getRecordId(), courseRecordStudentDTO.getSeatIndex());
        if (occupiedCount != null && occupiedCount > 0) {
            throw new BusinessException(CourseRecordStudentEnum.SEAT_ALREADY_OCCUPIED);
        }

        // 创建座位记录
        CourseRecordStudent student = new CourseRecordStudent();
        BeanUtils.copyProperties(courseRecordStudentDTO, student);
        student.setId(UUID.randomUUID());
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());

        // 默认值
        if (student.getSeatStatus() == null) {
            student.setSeatStatus(3);
        }

        this.save(student);

        // 返回结果
        CourseRecordStudentVO studentVO = new CourseRecordStudentVO();
        BeanUtils.copyProperties(student, studentVO);
        return studentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecordStudent", key = "#p0.recordId + ':' + #p0.studentId", condition = "#p0.recordId != null and #p0.studentId != null"),
            @CacheEvict(value = "CourseRecordStudent", key = "'all'", condition = "true")
    })
    public Boolean updateStudentSeat(CourseRecordStudentDTO courseRecordStudentDTO) {
        if (courseRecordStudentDTO == null || courseRecordStudentDTO.getRecordId() == null || courseRecordStudentDTO.getStudentId() == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_AND_STUDENT_ID_REQUIRED);
        }

        // 检查记录是否存在
        LambdaQueryWrapper<CourseRecordStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseRecordStudent::getRecordId, courseRecordStudentDTO.getRecordId())
                .eq(CourseRecordStudent::getStudentId, courseRecordStudentDTO.getStudentId());
        CourseRecordStudent existingStudent = this.getOne(queryWrapper);
        if (existingStudent == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_STUDENT_NOT_EXISTS);
        }

        // 如果更换座位，检查新座位是否被占用
        if (courseRecordStudentDTO.getSeatIndex() != null && !courseRecordStudentDTO.getSeatIndex().equals(existingStudent.getSeatIndex())) {
            Integer occupiedCount = courseRecordStudentMapper.checkSeatOccupied(courseRecordStudentDTO.getRecordId(), courseRecordStudentDTO.getSeatIndex());
            if (occupiedCount != null && occupiedCount > 0) {
                throw new BusinessException(CourseRecordStudentEnum.SEAT_ALREADY_OCCUPIED);
            }
        }

        // 更新记录
        CourseRecordStudent student = new CourseRecordStudent();
        BeanUtils.copyProperties(courseRecordStudentDTO, student);
        student.setUpdateTime(LocalDateTime.now());

        return this.update(student, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseRecordStudent", key = "#p0 + ':' + #p1", condition = "#p0 != null and #p1 != null"),
            @CacheEvict(value = "CourseRecordStudent", key = "'all'", condition = "true")
    })
    public Boolean removeStudentSeat(UUID recordId, UUID studentId) {
        if (recordId == null || studentId == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_AND_STUDENT_ID_REQUIRED);
        }

        // 检查记录是否存在 - 使用不包含is_deleted条件的查询方式
        LambdaQueryWrapper<CourseRecordStudent> queryWrapper = new LambdaQueryWrapper<CourseRecordStudent>().eq(CourseRecordStudent::getRecordId, recordId)
                .eq(CourseRecordStudent::getStudentId, studentId);
        CourseRecordStudent student = this.baseMapper.selectOne(queryWrapper);
        if (student == null) {
            throw new BusinessException(CourseRecordStudentEnum.COURSE_RECORD_STUDENT_NOT_EXISTS);
        }

        // 直接使用baseMapper删除，避免自动添加is_deleted条件
        return this.baseMapper.delete(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "CourseRecordStudent", allEntries = true)
    public Integer removeStudentSeatBatch(List<StudentSeatDeleteDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            throw new BusinessException(CourseRecordStudentEnum.BATCH_LIST_REQUIRED);
        }

        int count = 0;
        StringBuilder errorMsg = new StringBuilder();
        for (StudentSeatDeleteDTO dto : dtoList) {
            try {
                if (removeStudentSeat(dto.getRecordId(), dto.getStudentId())) {
                    count++;
                }
            } catch (BusinessException e) {
                String msg = String.format("recordId=%s, studentId=%s: %s",
                        dto.getRecordId(), dto.getStudentId(), e.getMessage());
                errorMsg.append(msg).append("; ");
                log.warn("删除学生座位失败: {}", msg);
            }
        }

        // 如果全部删除失败，抛出异常，确保前后端响应一致
        if (count == 0 && !dtoList.isEmpty()) {
            throw new BusinessException("批量删除学生座位失败: " + errorMsg.toString());
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkSeatOccupied(UUID recordId, Integer seatIndex) {
        if (recordId == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_REQUIRED);
        }
        if (seatIndex == null) {
            throw new BusinessException(CourseRecordStudentEnum.SEAT_INDEX_REQUIRED);
        }

        Integer count = courseRecordStudentMapper.checkSeatOccupied(recordId, seatIndex);
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countStudentsByRecordId(UUID recordId) {
        if (recordId == null) {
            throw new BusinessException(CourseRecordStudentEnum.RECORD_ID_REQUIRED);
        }

        Integer count = courseRecordStudentMapper.countStudentsByRecordId(recordId);
        return count != null ? count : 0;
    }
}
