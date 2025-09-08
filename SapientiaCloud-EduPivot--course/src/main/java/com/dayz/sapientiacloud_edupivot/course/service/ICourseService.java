package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
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

    Boolean removeCourseById(UUID courseId);

    Integer removeCourseByIds(List<UUID> courseIds);

    Boolean assignTeacher(UUID courseId, UUID teacherId);

    Boolean enrollStudentToCourse(UUID courseId, UUID studentId);

    Boolean assignCourseTeacherTeam(UUID courseId, List<UUID> teacherIds);

    List<CourseVO> listAllCourseByStudentId(UUID studentId);

    List<CourseVO> listAllCourseByTeacherId(UUID teacherId);

    Boolean assignCourseTeachers(UUID courseId, List<UUID> teacherIds);

    PageInfo<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO);

    PageInfo<CourseVO> listCourseByStudentId(CourseStudentQueryDTO courseStudentQueryDTO);
}
