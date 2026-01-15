package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.MyCourseForTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseTeacherService {

    PageInfo<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO);

    List<TeacherVO> listAllTeacherByCourseId(UUID courseId);

    List<CourseVO> listAllCourseByTeacherId(UUID teacherId);

    PageInfo<CourseVO> listMyCourseForTeacher(MyCourseForTeacherQueryDTO myCourseForTeacherQueryDTO);

    Boolean batchAddAssistantTeachers(UUID courseId, List<UUID> teacherIds);

    Integer batchDeleteAssistantTeachers(UUID courseId, List<UUID> teacherIds);

    Boolean assignCourseTeachers(UUID courseId, List<UUID> teacherIds);
}
