package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.MyCourseForStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.MyCourseVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseStudentService {

    PageInfo<CourseStudentVO> listCourseStudent(CourseStudentQueryDTO courseStudentQueryDTO);

    PageInfo<MyCourseVO> listMyCourseForStudent(MyCourseForStudentQueryDTO myCourseForStudentQueryDTO);

    List<CourseStudentVO> listAllCourseStudentByCourseId(UUID courseId);

    List<CourseStudentVO> listAllCourseStudentByStudentId(UUID studentId);

    CourseStudentVO getStudentCourseById(UUID studentId, UUID courseId);

    Boolean addCourseStudent(CourseStudentDTO courseStudentDTO);

    Boolean updateCourseStudent(CourseStudentDTO courseStudentDTO);

    Boolean removeCourseStudentById(UUID courseId, UUID studentId);

    Integer removeCourseStudentByIds(UUID courseId, List<UUID> ids);

    List<CourseStudentVO> listAllCourseStudentByCourseIds(List<UUID> courseIds);
}
