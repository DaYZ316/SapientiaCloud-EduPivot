package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.PublicCourseVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseService;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    private static final String PUBLIC_COURSE_CACHE_KEY = "PublicCourse::random:six";
    private static final Duration PUBLIC_COURSE_CACHE_TTL = Duration.ofHours(24);

    private final CourseMapper courseMapper;
    private final ICourseStudentService courseStudentService;
    private final RedisTemplate<String, Object> redisTemplate;

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
    @Caching(evict = {
            @CacheEvict(value = "Course", allEntries = true),
            @CacheEvict(value = "CourseTeacher", allEntries = true)
    })
    public CourseVO addCourse(CourseDTO courseDTO) {
        if (courseDTO == null) {
            throw new BusinessException(CourseEnum.COURSE_INFO_REQUIRED);
        }

        validateIsPublic(courseDTO.getIsPublic());

        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCourseName, courseDTO.getCourseName());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException(CourseEnum.COURSE_NAME_EXISTS);
        }

        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        if (course.getIsPublic() == null) {
            course.setIsPublic(CourseConstants.DEFAULT_IS_PUBLIC);
        }

        course.setId(UuidCreator.getTimeOrderedEpoch());
        course.setStatus(StatusEnum.NORMAL.getCode());
        course.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        this.save(course);
        clearPublicCourseCache();

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

        // 检查是否尝试更新公开状态，如果公开字段不一样，则抛出异常
        if (courseDTO.getIsPublic() != null && !courseDTO.getIsPublic().equals(existingCourse.getIsPublic())) {
            throw new BusinessException(CourseEnum.COURSE_PUBLIC_STATUS_CANNOT_UPDATE);
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
        // 确保公开状态不被更新，保持原值
        course.setIsPublic(existingCourse.getIsPublic());
        course.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(course);
        if (updated) {
            clearPublicCourseCache();
        }
        return updated;
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

        boolean removed = this.removeById(courseId);
        if (removed) {
            clearPublicCourseCache();
        }
        return removed;
    }

    // 弃用
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Course", allEntries = true),
            @CacheEvict(value = "CourseTeacher", allEntries = true)
    })
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
        if (removeResult) {
            clearPublicCourseCache();
        }
        return Math.toIntExact(removeResult ? courseIds.size() : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicCourseVO> listPublicCourse() {
        // 尝试从缓存获取
        Object cached = redisTemplate.opsForValue().get(PUBLIC_COURSE_CACHE_KEY);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            List<PublicCourseVO> result = (List<PublicCourseVO>) cached;
            return result;
        }

        // 缓存未命中，从数据库查询
        List<PublicCourseVO> result = courseMapper.listPublicCourse(CourseConstants.IS_PUBLIC_MAX);

        // 存入缓存，设置24小时过期时间
        if (result != null) {
            redisTemplate.opsForValue().set(PUBLIC_COURSE_CACHE_KEY, result, PUBLIC_COURSE_CACHE_TTL);
        }

        return result;
    }

    private void validateIsPublic(Integer isPublic) {
        if (isPublic == null) {
            return;
        }
        if (isPublic < CourseConstants.IS_PUBLIC_MIN || isPublic > CourseConstants.IS_PUBLIC_MAX) {
            throw new BusinessException(CourseEnum.COURSE_PUBLIC_STATUS_INVALID);
        }
    }

    private void clearPublicCourseCache() {
        redisTemplate.delete(PUBLIC_COURSE_CACHE_KEY);
    }
}
