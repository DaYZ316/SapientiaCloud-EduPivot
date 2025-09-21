package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.TeacherVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    List<CourseVO> listCourse(CourseQueryDTO courseQueryDTO);

    List<CourseVO> listAllCourse();

    List<TeacherVO> listAllTeacherByCourseId(List<UUID> teacherIds);

    List<CourseVO> listAllCourseByTeacherId(@Param("teacherId") UUID teacherId);

    List<CourseVO> listCourseByTeacherId(CourseTeacherQueryDTO courseTeacherQueryDTO);

    CourseVO getCourseById(@Param("courseId") UUID courseId);
}
