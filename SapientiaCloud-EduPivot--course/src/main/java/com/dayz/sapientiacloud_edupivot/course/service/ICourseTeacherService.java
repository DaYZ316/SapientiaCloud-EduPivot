package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseTeacherService {

    PageInfo<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO);

    List<TeacherVO> listAllTeacherByCourseId(UUID courseId);

    List<CourseVO> listAllCourseByTeacherId(UUID teacherId);

    Boolean assignTeacher(UUID courseId, UUID teacherId);

    Boolean assignCourseTeachers(UUID courseId, List<UUID> teacherIds);
}
