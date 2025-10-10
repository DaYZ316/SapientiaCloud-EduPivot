package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseForumVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseForumService {

    PageInfo<CourseForumVO> listCourseForum(CourseForumQueryDTO courseForumQueryDTO);

    List<CourseForumVO> listAllCourseForumByCourseId(UUID courseId);

    CourseForumVO getCourseForumById(UUID id);

    CourseForumVO addCourseForum(CourseForumDTO courseForumDTO);

    Boolean updateCourseForum(CourseForumDTO courseForumDTO);

    Boolean removeCourseForumById(UUID id);

    Integer removeCourseForumByIds(List<UUID> ids);

    Boolean updateForumStatus(UUID id, Integer status);

    Boolean setForumModerators(UUID id, List<UUID> moderatorIds);

    CourseForumVO getForumStatistics(UUID id);
}
