package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.StudentCourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.StudentCourseVO;
import com.github.pagehelper.PageInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IStudentCourseService {

    Boolean enrollCourse(StudentCourseDTO studentCourseDTO);

    Boolean dropCourse(UUID studentId, UUID courseId);

    Boolean updateGrade(UUID studentId, UUID courseId, java.math.BigDecimal grade);

    Integer batchUpdateGrade(List<StudentCourseDTO> studentCourseDTOList);

    PageInfo<StudentCourseVO> listStudentCoursePageByStudentId(UUID studentId, Integer pageNum, Integer pageSize);

    PageInfo<StudentCourseVO> listStudentCoursePageByCourseId(UUID courseId, Integer pageNum, Integer pageSize);

    List<StudentCourseVO> listStudentCourseByStudentId(UUID studentId);

    List<StudentCourseVO> listStudentCourseByCourseId(UUID courseId);

    Boolean isEnrolled(UUID studentId, UUID courseId);

    BigDecimal getStudentGrade(UUID studentId, UUID courseId);
}
