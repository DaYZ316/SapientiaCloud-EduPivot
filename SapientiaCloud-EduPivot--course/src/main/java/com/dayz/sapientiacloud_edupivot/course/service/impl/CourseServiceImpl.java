package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseService;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    private final CourseMapper courseMapper;
    private final ICourseStudentService courseStudentService;

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
    @Cacheable(value = "Course", key = "'all'", condition = "true")
    public List<CourseVO> listAllCourse() {
        return courseMapper.listAllCourse();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Course", key = "#p0", condition = "#p0 != null")
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
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
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

        course.setId(UuidCreator.getTimeOrderedEpoch());
        course.setStatus(StatusEnum.NORMAL.getCode());
        course.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        this.save(course);

        CourseVO courseVO = new CourseVO();
        BeanUtils.copyProperties(course, courseVO);
        return courseVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Course", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "Course", key = "'all'", condition = "true"),
            @CacheEvict(value = "CourseTeacher", allEntries = true)
    })
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
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Course", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "Course", key = "'all'", condition = "true"),
            @CacheEvict(value = "CourseTeacher", allEntries = true)
    })
    public Boolean removeCourseById(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        List<CourseStudentVO> courseStudents = courseStudentService.listAllCourseStudentByCourseId(courseId);
        if (courseStudents != null && !courseStudents.isEmpty()) {
            throw new BusinessException(CourseEnum.COURSE_HAS_STUDENTS);
        }

        return this.removeById(courseId);
    }

    // 弃用
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
    public Integer removeCourseByIds(List<UUID> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new BusinessException(CourseEnum.COURSE_ID_LIST_REQUIRED);
        }

        List<Course> courses = this.listByIds(courseIds);
        courseIds.forEach(courseId -> {
            if (courses.stream().noneMatch(course -> course.getId().equals(courseId))) {
                throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
            }
        });

        List<CourseStudentVO> allCourseStudents = courseStudentService.listAllCourseStudentByCourseIds(courseIds);
        if (allCourseStudents != null && !allCourseStudents.isEmpty()) {
            throw new BusinessException(CourseEnum.COURSE_HAS_STUDENTS);
        }

        boolean removeResult = this.removeBatchByIds(courseIds);
        return Math.toIntExact(removeResult ? courseIds.size() : 0);
    }
}
