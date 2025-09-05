package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseService {

    PageInfo<CourseVO> listCoursePage(CourseQueryDTO courseQueryDTO);

    List<CourseVO> listAllCourse();

    CourseVO getCourseById(UUID courseId);

    CourseVO addCourse(CourseDTO courseDTO);

    Boolean updateCourse(CourseDTO courseDTO);

    Boolean removeCourse(UUID courseId);

    Integer removeCourses(List<UUID> courseIds);

    List<CourseVO> listCourseByTeacherId(UUID teacherId);

    List<CourseVO> listAvailableCourseByStudentId(UUID studentId);

    Boolean updateCourseStatus(UUID courseId, Integer status);

    Boolean assignTeacher(UUID courseId, UUID teacherId);
}
