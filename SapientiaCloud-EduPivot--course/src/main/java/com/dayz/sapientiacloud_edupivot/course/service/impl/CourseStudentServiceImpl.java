package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseStudent;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseChapterEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.EnrollmentStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseStudentMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseStudentServiceImpl extends ServiceImpl<CourseStudentMapper, CourseStudent> implements ICourseStudentService {

    private final CourseStudentMapper courseStudentMapper;

    @Override
    public PageInfo<CourseStudentVO> listCourseStudent(CourseStudentQueryDTO courseStudentQueryDTO) {
        if (courseStudentQueryDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_REQUIRED);
        }

        return PageHelper.startPage(courseStudentQueryDTO.getPageNum(), courseStudentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseStudentMapper.listCourseStudent(courseStudentQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }

        return courseStudentMapper.listAllCourseStudentByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseStudentVO getStudentCourseById(UUID studentId, UUID courseId) {
        if (studentId == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        CourseStudentVO courseStudentVO = courseStudentMapper.getStudentCourseById(studentId, courseId);
        if (courseStudentVO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        return courseStudentVO;
    }

    @Override
    @Transactional
    public Boolean addCourseStudent(CourseStudentDTO courseStudentDTO) {
        if (courseStudentDTO == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_INFO_REQUIRED);
        }

        if (courseStudentDTO.getStudentId() == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }
        if (courseStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        // 检查是否已经选课
        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, courseStudentDTO.getStudentId())
                .eq(CourseStudent::getCourseId, courseStudentDTO.getCourseId())
                .eq(CourseStudent::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NAME_EXISTS);
        }

        CourseStudent courseStudent = new CourseStudent();
        BeanUtils.copyProperties(courseStudentDTO, courseStudent);

        // 设置默认值
        if (courseStudent.getStatus() == null) {
            courseStudent.setStatus(EnrollmentStatusEnum.ENROLLED.getCode());
        }
        if (courseStudent.getEnrollmentDate() == null) {
            courseStudent.setEnrollmentDate(LocalDate.now());
        }

        courseStudent.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        courseStudent.setCreateTime(LocalDateTime.now());
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.save(courseStudent);
    }

    @Override
    @Transactional
    public Boolean updateCourseStudent(CourseStudentDTO courseStudentDTO) {
        if (courseStudentDTO == null || courseStudentDTO.getStudentId() == null || courseStudentDTO.getCourseId() == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_INFO_OR_ID_REQUIRED);
        }

        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, courseStudentDTO.getStudentId())
                .eq(CourseStudent::getCourseId, courseStudentDTO.getCourseId())
                .eq(CourseStudent::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        CourseStudent existingCourseStudent = this.getOne(queryWrapper);
        if (existingCourseStudent == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        CourseStudent courseStudent = new CourseStudent();
        BeanUtils.copyProperties(courseStudentDTO, courseStudent);
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseStudent);
    }

    @Override
    @Transactional
    public Boolean removeCourseStudentById(UUID courseId, UUID studentId) {
        if (studentId == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_REQUIRED);
        }
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }

        LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseStudent::getStudentId, studentId)
                .eq(CourseStudent::getCourseId, courseId)
                .eq(CourseStudent::getDeleted, DeletedEnum.NOT_DELETED.getCode());

        CourseStudent courseStudent = this.getOne(queryWrapper);
        if (courseStudent == null) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_NOT_EXISTS);
        }

        courseStudent.setDeleted(DeletedEnum.DELETED.getCode());
        courseStudent.setUpdateTime(LocalDateTime.now());

        return this.updateById(courseStudent);
    }

    @Override
    @Transactional
    public Integer removeCourseStudentByIds(UUID courseId, List<UUID> studentIds) {
        if (courseId == null) {
            throw new BusinessException(CourseChapterEnum.COURSE_ID_REQUIRED);
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BusinessException(CourseChapterEnum.CHAPTER_ID_LIST_REQUIRED);
        }

        // 批量逻辑删除
        int deleteCount = 0;
        for (UUID studentId : studentIds) {
            LambdaQueryWrapper<CourseStudent> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseStudent::getStudentId, studentId)
                    .eq(CourseStudent::getCourseId, courseId)
                    .eq(CourseStudent::getDeleted, DeletedEnum.NOT_DELETED.getCode());

            CourseStudent courseStudent = this.getOne(queryWrapper);
            if (courseStudent != null) {
                courseStudent.setDeleted(DeletedEnum.DELETED.getCode());
                courseStudent.setUpdateTime(LocalDateTime.now());
                if (this.updateById(courseStudent)) {
                    deleteCount++;
                }
            }
        }

        return deleteCount;
    }
}
