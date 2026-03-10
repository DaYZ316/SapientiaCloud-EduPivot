package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.MyCourseForTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseTeacherService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseAssistantTeacher;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseAssistantTeacherMapper;

@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseTeacherService {

    private final CourseMapper courseMapper;
    private final CourseAssistantTeacherMapper courseAssistantTeacherMapper;

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

        return courseAssistantTeacherMapper.listAssistantByCourseId(courseId);
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

        // 目标集合：去重并确保包含主讲教师（与 CourseServiceImpl 保持一致）
        List<UUID> targetList = new ArrayList<>(new java.util.HashSet<>(teacherIds));
        UUID mainTeacherId = course.getTeacherId();
        if (mainTeacherId != null && !targetList.contains(mainTeacherId)) {
            targetList.add(mainTeacherId);
        }

        // 现有集合
        List<UUID> existingAssistants = courseAssistantTeacherMapper.listAssistantIdsByCourseId(courseId);

        // 计算待删除和待新增
        List<UUID> toRemove = existingAssistants.stream()
                .filter(id -> !targetList.contains(id))
                .collect(Collectors.toList());
        List<UUID> toAdd = targetList.stream()
                .filter(id -> !existingAssistants.contains(id))
                .toList();

        if (!toRemove.isEmpty()) {
            courseAssistantTeacherMapper.deleteAssistantTeachers(courseId, toRemove);
        }
        if (!toAdd.isEmpty()) {
            List<CourseAssistantTeacher> list = toAdd.stream().map(tid -> {
                CourseAssistantTeacher at = new CourseAssistantTeacher();
                at.setCourseId(courseId);
                at.setAssistantTeacherId(tid);
                at.setCreateTime(LocalDateTime.now());
                return at;
            }).collect(Collectors.toList());
            courseAssistantTeacherMapper.batchInsertAssistantTeachers(list);
        }

        // 更新课程更新时间
        course.setUpdateTime(LocalDateTime.now());
        this.updateById(course);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseVO> listMyCourseForTeacher(MyCourseForTeacherQueryDTO myCourseForTeacherQueryDTO) {
        if (myCourseForTeacherQueryDTO == null) {
            throw new BusinessException(CourseEnum.TEACHER_ID_REQUIRED);
        }

        return PageHelper.startPage(myCourseForTeacherQueryDTO.getPageNum(), myCourseForTeacherQueryDTO.getPageSize())
                .doSelectPageInfo(() -> courseAssistantTeacherMapper.listCourseByAssistant(myCourseForTeacherQueryDTO));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
    public Boolean batchAddAssistantTeachers(UUID courseId, List<UUID> teacherIds) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }
        if (teacherIds == null || teacherIds.isEmpty()) {
            throw new BusinessException(CourseEnum.TEACHER_ID_LIST_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        List<CourseAssistantTeacher> list = teacherIds.stream().map(tid -> {
            CourseAssistantTeacher at = new CourseAssistantTeacher();
            at.setCourseId(courseId);
            at.setAssistantTeacherId(tid);
            at.setCreateTime(LocalDateTime.now());
            return at;
        }).toList();

        int inserted = courseAssistantTeacherMapper.batchInsertAssistantTeachers(list);
        return inserted > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"Course", "CourseTeacher"}, allEntries = true)
    public Integer batchDeleteAssistantTeachers(UUID courseId, List<UUID> teacherIds) {
        if (courseId == null) {
            throw new BusinessException(CourseEnum.COURSE_ID_REQUIRED);
        }
        if (teacherIds == null || teacherIds.isEmpty()) {
            throw new BusinessException(CourseEnum.TEACHER_ID_LIST_REQUIRED);
        }

        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        return courseAssistantTeacherMapper.deleteAssistantTeachers(courseId, teacherIds);
    }
}
