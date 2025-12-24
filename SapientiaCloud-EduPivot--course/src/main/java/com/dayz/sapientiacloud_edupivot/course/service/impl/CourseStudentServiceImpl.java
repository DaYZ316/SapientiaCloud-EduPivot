package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseStudent;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseStudentEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.EnrollmentStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseStudentMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.MyCourseForStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.MyCourseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseStudentServiceImpl extends ServiceImpl<CourseStudentMapper, CourseStudent> implements ICourseStudentService {

    private final CourseStudentMapper courseStudentMapper;

    @Override
    public PageInfo<CourseStudentVO> listCourseStudent(CourseStudentQueryDTO courseStudentQueryDTO) {
        if (courseStudentQueryDTO == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_REQUIRED);
        }

        return PageHelper.startPage(courseStudentQueryDTO.getPageNum(), courseStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseStudentMapper.listCourseStudent(courseStudentQueryDTO));
    }

    @Override
    public PageInfo<MyCourseVO> listMyCourseForStudent(MyCourseForStudentQueryDTO myCourseForStudentQueryDTO) {
        if (myCourseForStudentQueryDTO == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_REQUIRED);
        }

        return PageHelper.startPage(myCourseForStudentQueryDTO.getPageNum(), myCourseForStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseStudentMapper.listMyCourseForStudent(myCourseForStudentQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseStudent", key = "'course_' + #p0", condition = "#p0 != null")
    public List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseStudent", key = "'student_' + #p0", condition = "#p0 != null")
    public List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseStudentEnum.STUDENT_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseStudent", key = "#p0 + '_' + #p1", condition = "#p0 != null && #p1 != null")
    public CourseStudentVO getStudentCourseById(UUID studentId, UUID courseId) {
        if (studentId == null) {
            throw new BusinessException(CourseStudentEnum.STUDENT_ID_REQUIRED);
        }
        if (courseId == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }

        CourseStudentVO courseStudentVO = courseStudentMapper.getStudentCourseById(studentId, courseId);
        if (courseStudentVO == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_NOT_EXISTS);
        }

        return courseStudentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "CourseStudent", allEntries = true)
    public Boolean addCourseStudent(CourseStudentDTO courseStudentDTO) {
        if (courseStudentDTO == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_INFO_REQUIRED);
        }

        if (courseStudentDTO.getStudentId() == null) {
            throw new BusinessException(CourseStudentEnum.STUDENT_ID_REQUIRED);
        }
        if (courseStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }

        // 检查是否已经选课
        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, courseStudentDTO.getStudentId())
                .eq(CourseStudent::getCourseId, courseStudentDTO.getCourseId());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseStudentEnum.ALREADY_ENROLLED);
        }

        CourseStudent courseStudent = new CourseStudent();
        BeanUtils.copyProperties(courseStudentDTO, courseStudent);

        // 设置默认值
        if (courseStudent.getStatus() == null) {
            courseStudent.setStatus(EnrollmentStatusEnum.ENROLLED.getCode());
        }

        courseStudent.setCreateTime(LocalDateTime.now());
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.save(courseStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseStudent", key = "#p0.studentId + '_' + #p0.courseId", condition = "#p0.studentId != null && #p0.courseId != null"),
            @CacheEvict(value = "CourseStudent", key = "'course_' + #p0.courseId", condition = "#p0.courseId != null"),
            @CacheEvict(value = "CourseStudent", key = "'student_' + #p0.studentId", condition = "#p0.studentId != null")
    })
    public Boolean updateCourseStudent(CourseStudentDTO courseStudentDTO) {
        if (courseStudentDTO == null || courseStudentDTO.getStudentId() == null || courseStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_INFO_OR_ID_REQUIRED);
        }

        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, courseStudentDTO.getStudentId())
                .eq(CourseStudent::getCourseId, courseStudentDTO.getCourseId());

        CourseStudent existingCourseStudent = this.getOne(queryWrapper);
        if (existingCourseStudent == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_NOT_EXISTS);
        }

        CourseStudent courseStudent = new CourseStudent();
        BeanUtils.copyProperties(courseStudentDTO, courseStudent);
        courseStudent.setUpdateTime(LocalDateTime.now());

        return courseStudentMapper.updateCourseStudent(courseStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "CourseStudent", key = "#p1 + '_' + #p0", condition = "#p0 != null && #p1 != null"),
            @CacheEvict(value = "CourseStudent", key = "'course_' + #p0", condition = "#p0 != null"),
            @CacheEvict(value = "CourseStudent", key = "'student_' + #p0", condition = "#p0 != null")
    })
    public Boolean removeCourseStudentById(UUID courseId, UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseStudentEnum.STUDENT_ID_REQUIRED);
        }
        if (courseId == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }

        return courseStudentMapper.removeCourseStudentById(studentId, courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "CourseStudent", allEntries = true)
    public Integer removeCourseStudentByIds(UUID courseId, List<UUID> studentIds) {
        if (courseId == null) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException(CourseStudentEnum.COURSE_STUDENT_ID_LIST_REQUIRED);
        }

        return courseStudentMapper.removeCourseStudentByIds(courseId, studentIds);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseStudent", key = "'courses_' + #p0.hashCode()", condition = "#p0 != null && !#p0.isEmpty()")
    public List<CourseStudentVO> listAllCourseStudentByCourseIds(List<UUID> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            throw new BusinessException(CourseStudentEnum.COURSE_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByCourseIds(courseIds);
    }
}
