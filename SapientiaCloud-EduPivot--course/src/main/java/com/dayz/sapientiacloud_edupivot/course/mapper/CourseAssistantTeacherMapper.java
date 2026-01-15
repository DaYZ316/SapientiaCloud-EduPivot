package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseAssistantTeacher;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseAssistantTeacherMapper {

    List<CourseVO> listCourseByAssistantId(@Param("assistantTeacherId") UUID assistantTeacherId);

    int batchInsertAssistantTeachers(@Param("list") List<CourseAssistantTeacher> list);

    int deleteAssistantTeachers(@Param("courseId") UUID courseId, @Param("teacherIds") List<UUID> teacherIds);

    int deleteAssistantTeachersByCourseId(@Param("courseId") UUID courseId);

    List<UUID> listAssistantIdsByCourseId(@Param("courseId") UUID courseId);

    List<TeacherVO> listAssistantByCourseId(@Param("courseId") UUID courseId);
}


