package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    List<CourseVO> listCourse(CourseQueryDTO courseQueryDTO);

    List<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO);

    List<CourseVO> listCourseByStudentId(CourseStudentQueryDTO courseStudentQueryDTO);

    CourseVO getCourseById(@Param("courseId") UUID courseId);

    int updateEnrolledCount(@Param("courseId") UUID courseId, @Param("increment") int increment);

    List<CourseVO> listAllCourse();

    Boolean enrollStudentToCourse(@Param("courseId") UUID courseId, @Param("studentId") UUID studentId);

    List<CourseVO> listAllCourseByStudentId(@Param("studentId") UUID studentId);

    List<CourseVO> listAllCourseByTeacherId(@Param("teacherId") UUID teacherId);
}
