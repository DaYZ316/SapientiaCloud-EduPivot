package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseStudent;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseStudentMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseStudentServiceImpl extends ServiceImpl<CourseStudentMapper, CourseStudent> implements ICourseStudentService {

    private final CourseStudentMapper courseStudentMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public Boolean enrollCourse(CourseStudentDTO courseStudentDTO) {
        if (courseStudentDTO == null || courseStudentDTO.getStudentId() == null || courseStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_INFO_INCOMPLETE);
        }

        if (isEnrolled(courseStudentDTO.getStudentId(), courseStudentDTO.getCourseId())) {
            throw new BusinessException(CourseEnum.ALREADY_ENROLLED);
        }

        Course course = courseMapper.selectById(courseStudentDTO.getCourseId());
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        if (course.getStatus() != 0) {
            throw new BusinessException(CourseEnum.COURSE_NOT_AVAILABLE);
        }

        CourseStudent courseStudent = new CourseStudent();
        BeanUtils.copyProperties(courseStudentDTO, courseStudent);

        courseStudent.setEnrollmentDate(LocalDate.now());
        courseStudent.setStatus(StatusEnum.NORMAL.getCode());
        courseStudent.setCreateTime(LocalDateTime.now());
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.save(courseStudent);
    }

    @Override
    @Transactional
    public Boolean dropCourse(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_AND_COURSE_ID_REQUIRED);
        }

        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, studentId)
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getStatus, StatusEnum.DISABLED);

        CourseStudent courseStudent = this.getOne(queryWrapper);
        if (courseStudent == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_RECORD_NOT_FOUND);
        }

        courseStudent.setStatus(StatusEnum.NORMAL.getCode());
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseStudent);
    }

    @Override
    @Transactional
    public Boolean updateGrade(UUID studentId, UUID courseId, BigDecimal grade) {
        if (studentId == null || courseId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_AND_COURSE_ID_REQUIRED);
        }
        if (grade == null || grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(CourseEnum.GRADE_INVALID_RANGE);
        }

        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, studentId)
                .eq(CourseStudent::getCourseId, courseId);

        CourseStudent courseStudent = this.getOne(queryWrapper);
        if (courseStudent == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_RECORD_NOT_EXISTS);
        }

        courseStudent.setGrade(grade);
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseStudent);
    }

    @Override
    @Transactional
    public Integer batchUpdateGrade(List<CourseStudentDTO> courseStudentDTOList) {
        if (courseStudentDTOList == null || courseStudentDTOList.isEmpty()) {
            throw new BusinessException(CourseEnum.GRADE_UPDATE_LIST_REQUIRED);
        }

        int updateCount = 0;
        for (CourseStudentDTO dto : courseStudentDTOList) {
            if (dto.getStudentId() != null && dto.getCourseId() != null && dto.getGrade() != null) {
                try {
                    if (updateGrade(dto.getStudentId(), dto.getCourseId(), dto.getGrade())) {
                        updateCount++;
                    }
                } catch (Exception e) {
                    log.warn("批量更新成绩失败: studentId={}, courseId={}, error={}",
                            dto.getStudentId(), dto.getCourseId(), e.getMessage());
                }
            }
        }

        return updateCount;
    }

    @Override
    public PageInfo<CourseStudentVO> listCourseStudentByStudentId(CourseStudentQueryDTO courseStudentQueryDTO) {
        if (courseStudentQueryDTO == null || courseStudentQueryDTO.getStudentId() == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_REQUIRED);
        }

        return PageHelper.startPage(courseStudentQueryDTO.getPageNum(), courseStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseStudentMapper.listCourseStudentByStudentId(courseStudentQueryDTO));
    }

    @Override
    public PageInfo<CourseStudentVO> listCourseStudentByCourseId(CourseStudentQueryDTO courseStudentQueryDTO) {
        if (courseStudentQueryDTO == null || courseStudentQueryDTO.getCourseId() == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        return PageHelper.startPage(courseStudentQueryDTO.getPageNum(), courseStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseStudentMapper.listCourseStudentByCourseId(courseStudentQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isEnrolled(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            return false;
        }

        return courseStudentMapper.countByStudentAndCourse(studentId, courseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStudentGrade(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            return null;
        }

        CourseStudentVO courseStudent = courseStudentMapper.getCourseStudentByStudentAndCourse(studentId, courseId);
        return courseStudent != null ? courseStudent.getGrade() : null;
    }
}
