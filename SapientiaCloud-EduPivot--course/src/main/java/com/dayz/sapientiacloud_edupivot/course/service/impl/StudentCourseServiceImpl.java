package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.StudentCourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.StudentCourseVO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.po.StudentCourse;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.StudentCourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.IStudentCourseService;
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
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseMapper, StudentCourse> implements IStudentCourseService {

    private final StudentCourseMapper studentCourseMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public Boolean enrollCourse(StudentCourseDTO studentCourseDTO) {
        if (studentCourseDTO == null || studentCourseDTO.getStudentId() == null || studentCourseDTO.getCourseId() == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_INFO_INCOMPLETE);
        }

        if (isEnrolled(studentCourseDTO.getStudentId(), studentCourseDTO.getCourseId())) {
            throw new BusinessException(CourseEnum.ALREADY_ENROLLED);
        }

        Course course = courseMapper.selectById(studentCourseDTO.getCourseId());
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        if (course.getStatus() != 0) {
            throw new BusinessException(CourseEnum.COURSE_NOT_AVAILABLE);
        }

        StudentCourse studentCourse = new StudentCourse();
        BeanUtils.copyProperties(studentCourseDTO, studentCourse);
        
        studentCourse.setEnrollmentDate(LocalDate.now());
        studentCourse.setStatus(StatusEnum.NORMAL.getCode());
        studentCourse.setCreateTime(LocalDateTime.now());
        studentCourse.setUpdateTime(LocalDateTime.now());

        return this.save(studentCourse);
    }

    @Override
    @Transactional
    public Boolean dropCourse(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_AND_COURSE_ID_REQUIRED);
        }

        LambdaQueryWrapper<StudentCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentCourse::getStudentId, studentId)
                   .eq(StudentCourse::getCourseId, courseId)
                   .eq(StudentCourse::getStatus, StatusEnum.DISABLED);

        StudentCourse studentCourse = this.getOne(queryWrapper);
        if (studentCourse == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_RECORD_NOT_FOUND);
        }

        studentCourse.setStatus(StatusEnum.NORMAL.getCode());
        studentCourse.setUpdateTime(LocalDateTime.now());

        return this.updateById(studentCourse);
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

        LambdaQueryWrapper<StudentCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentCourse::getStudentId, studentId)
                   .eq(StudentCourse::getCourseId, courseId);

        StudentCourse studentCourse = this.getOne(queryWrapper);
        if (studentCourse == null) {
            throw new BusinessException(CourseEnum.ENROLLMENT_RECORD_NOT_EXISTS);
        }

        studentCourse.setGrade(grade);
        studentCourse.setUpdateTime(LocalDateTime.now());

        return this.updateById(studentCourse);
    }

    @Override
    @Transactional
    public Integer batchUpdateGrade(List<StudentCourseDTO> studentCourseDTOList) {
        if (studentCourseDTOList == null || studentCourseDTOList.isEmpty()) {
            throw new BusinessException(CourseEnum.GRADE_UPDATE_LIST_REQUIRED);
        }

        int updateCount = 0;
        for (StudentCourseDTO dto : studentCourseDTOList) {
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
    public PageInfo<StudentCourseVO> listStudentCoursePageByStudentId(UUID studentId, Integer pageNum, Integer pageSize) {
        if (studentId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_REQUIRED);
        }

        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> studentCourseMapper.listStudentCourseByStudentId(studentId));
    }

    @Override
    public PageInfo<StudentCourseVO> listStudentCoursePageByCourseId(UUID courseId, Integer pageNum, Integer pageSize) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(() -> studentCourseMapper.listStudentCourseByCourseId(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentCourseVO> listStudentCourseByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_REQUIRED);
        }

        return studentCourseMapper.listStudentCourseByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentCourseVO> listStudentCourseByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        return studentCourseMapper.listStudentCourseByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isEnrolled(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            return false;
        }

        return studentCourseMapper.countByStudentAndCourse(studentId, courseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getStudentGrade(UUID studentId, UUID courseId) {
        if (studentId == null || courseId == null) {
            return null;
        }

        StudentCourseVO studentCourse = studentCourseMapper.getStudentCourseByStudentAndCourse(studentId, courseId);
        return studentCourse != null ? studentCourse.getGrade() : null;
    }
}
