package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ICourseStudentService {

    Boolean enrollCourse(CourseStudentDTO courseStudentDTO);

    Boolean dropCourse(UUID studentId, UUID courseId);

    Boolean updateGrade(UUID studentId, UUID courseId, java.math.BigDecimal grade);

    Integer batchUpdateGrade(List<CourseStudentDTO> courseStudentDTOList);

    PageInfo<CourseStudentVO> listCourseStudentByStudentId(CourseStudentQueryDTO courseStudentQueryDTO);

    PageInfo<CourseStudentVO> listCourseStudentByCourseId(CourseStudentQueryDTO courseStudentQueryDTO);

    List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId);

    List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId);

    Boolean isEnrolled(UUID studentId, UUID courseId);

    BigDecimal getStudentGrade(UUID studentId, UUID courseId);
}
