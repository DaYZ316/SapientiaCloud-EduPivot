package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseThreadVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseThreadService {

    PageInfo<CourseThreadVO> listCourseThread(CourseThreadQueryDTO courseThreadQueryDTO);

    List<CourseThreadVO> listAllCourseThread();

    CourseThreadVO getCourseThreadById(UUID threadId);

    CourseThreadVO addCourseThread(CourseThreadDTO courseThreadDTO);

    Boolean updateCourseThread(CourseThreadDTO courseThreadDTO);

    Boolean removeCourseThreadById(UUID threadId);

    Integer removeCourseThreadByIds(List<UUID> threadIds);

    Boolean pinThread(UUID id, Boolean pinned);

    Boolean closeThread(UUID id, Boolean closed);

    Boolean viewThread(UUID id);
}
