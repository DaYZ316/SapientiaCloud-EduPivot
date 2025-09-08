package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseStudent;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseStudentMapper extends BaseMapper<CourseStudent> {

    List<CourseStudentVO> listAllCourseStudentByStudentId(@Param("studentId") UUID studentId);

    List<CourseStudentVO> listAllCourseStudentByCourseId(@Param("courseId") UUID courseId);

    List<CourseStudentVO> listCourseStudentByStudentId(CourseStudentQueryDTO courseStudentQueryDTO);

    List<CourseStudentVO> listCourseStudentByCourseId(CourseStudentQueryDTO courseStudentQueryDTO);

    CourseStudentVO getCourseStudentByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    int countByStudentAndCourse(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    int countByCourseId(@Param("courseId") UUID courseId);
}
