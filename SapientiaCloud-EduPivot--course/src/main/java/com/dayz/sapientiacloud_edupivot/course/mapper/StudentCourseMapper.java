package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.po.StudentCourse;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.StudentCourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    List<StudentCourseVO> listStudentCourseByStudentId(@Param("studentId") UUID studentId);

    List<StudentCourseVO> listStudentCourseByCourseId(@Param("courseId") UUID courseId);

    StudentCourseVO getStudentCourseByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    int countByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    int countByCourseId(@Param("courseId") UUID courseId);
}
