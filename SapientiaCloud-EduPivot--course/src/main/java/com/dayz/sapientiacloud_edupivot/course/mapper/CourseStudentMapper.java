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

    List<CourseStudentVO> listCourseStudent(CourseStudentQueryDTO courseStudentQueryDTO);

    List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId);

    List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId);

    CourseStudentVO getStudentCourseById(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

    Boolean addCourseStudent(CourseStudent courseStudent);

    Boolean updateCourseStudent(CourseStudent courseStudent);

    Boolean removeCourseStudentById(@Param("studentId") UUID courseId, @Param("courseId") UUID studentId);

    Integer removeCourseStudentByIds(@Param("courseId") UUID courseId, @Param("studentIds") List<UUID> studentIds);
}
