package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    private final CourseMapper courseMapper;

    @Override
    public PageInfo<CourseVO> listCoursePage(CourseQueryDTO courseQueryDTO) {
        if (courseQueryDTO == null) {
            throw new BusinessException(CourseEnum.COURSE_REQUIRED);
        }

        return PageHelper.startPage(courseQueryDTO.getPageNum(), courseQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseMapper.listCourse(courseQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> listAllCourse() {
        return courseMapper.listAllCourse();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseVO getCourseById(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        CourseVO courseVO = courseMapper.getCourseById(courseId);
        if (courseVO == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        return courseVO;
    }

    @Override
    @Transactional
    public CourseVO addCourse(CourseDTO courseDTO) {
        if (courseDTO == null) {
            throw new BusinessException(CourseEnum.COURSE_INFO_REQUIRED);
        }

        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseName, courseDTO.getCourseName());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseEnum.COURSE_NAME_EXISTS);
        }

        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);

        course.setId(UuidCreator.getTimeOrdered());
        course.setStatus(StatusEnum.NORMAL.getCode());
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        this.save(course);

        CourseVO courseVO = new CourseVO();
        BeanUtils.copyProperties(course, courseVO);
        return courseVO;
    }

    @Override
    @Transactional
    public Boolean updateCourse(CourseDTO courseDTO) {
        if (courseDTO == null || courseDTO.getId() == null) {
            throw new BusinessException(CourseEnum.COURSE_INFO_OR_ID_REQUIRED);
        }

        Course existingCourse = this.getById(courseDTO.getId());
        if (existingCourse == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        // 检查课程名称是否重复（排除自身）
        if (StringUtils.hasText(courseDTO.getCourseName()) && 
            !courseDTO.getCourseName().equals(existingCourse.getCourseName())) {
            LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Course::getCourseName, courseDTO.getCourseName())
                       .ne(Course::getId, courseDTO.getId());
            if (this.count(queryWrapper) > 0) {
                throw new BusinessException(CourseEnum.COURSE_NAME_EXISTS);
            }
        }

        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        course.setUpdateTime(LocalDateTime.now());

        return this.updateById(course);
    }

    @Override
    @Transactional
    public Boolean removeCourse(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        course.setDeleted(DeletedEnum.DELETED.getCode());
        course.setUpdateTime(LocalDateTime.now());

        return this.updateById(course);
    }

    @Override
    @Transactional
    public Integer removeCourses(List<UUID> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new BusinessException(CourseEnum.COURSE_ID_LIST_REQUIRED);
        }

        // 批量逻辑删除
        int deleteCount = 0;
        for (UUID courseId : courseIds) {
            Course course = this.getById(courseId);
            if (course != null) {
                course.setDeleted(DeletedEnum.DELETED.getCode());
                course.setUpdateTime(LocalDateTime.now());
                if (this.updateById(course)) {
                    deleteCount++;
                }
            }
        }

        return deleteCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> listCourseByTeacherId(UUID teacherId) {
        if (teacherId == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_REQUIRED);
        }

        return courseMapper.listCourseByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> listAvailableCourseByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseEnum.STUDENT_ID_REQUIRED);
        }

        return courseMapper.listCourseByStudentId(studentId);
    }

    @Override
    @Transactional
    public Boolean updateCourseStatus(UUID courseId, Integer status) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }
        if (status == null || status < 0 || status > 1) {
            throw new BusinessException(CourseEnum.COURSE_STATUS_INVALID);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        course.setStatus(status);
        course.setUpdateTime(LocalDateTime.now());

        return this.updateById(course);
    }

    @Override
    @Transactional
    public Boolean assignTeacher(UUID courseId, UUID teacherId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }
        if (teacherId == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        course.setTeacherId(teacherId);
        course.setUpdateTime(LocalDateTime.now());

        return this.updateById(course);
    }
}
