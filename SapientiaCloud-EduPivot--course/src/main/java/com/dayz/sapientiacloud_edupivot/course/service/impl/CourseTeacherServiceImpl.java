package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseTeacherService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseTeacherServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseTeacherService {

    private final CourseMapper courseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO) {
        if (courseTeacherQueryDTO == null || courseTeacherQueryDTO.getTeacherId() == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_REQUIRED);
        }

        return PageHelper.startPage(courseTeacherQueryDTO.getPageNum(), courseTeacherQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseMapper.listCourseByTeacherId(courseTeacherQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTeacher", key = "'courses_' + #p0", condition = "#p0 != null")
    public List<CourseVO> listAllCourseByTeacherId(UUID teacherId) {
        if (teacherId == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_REQUIRED);
        }

        return courseMapper.listAllCourseByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseTeacher", key = "'teachers_' + #p0", condition = "#p0 != null")
    public List<TeacherVO> listAllTeacherByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }

        CourseVO courseVO = courseMapper.getCourseById(courseId);
        if (courseVO == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        List<UUID> teacherIds = courseVO.getAssistantTeacherIds();
        if (teacherIds == null && courseVO.getTeacherId() != null) {
            teacherIds = new ArrayList<>();
        }
        teacherIds.add(courseVO.getTeacherId());

        return courseMapper.listAllTeacherByCourseId(teacherIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
    public Boolean assignCourseTeachers(UUID courseId, List<UUID> teacherIds) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }
        if (teacherIds == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_LIST_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        course.setAssistantTeacherIds(teacherIds);
        course.setUpdateTime(LocalDateTime.now());

        return this.updateById(course);
    }
}
