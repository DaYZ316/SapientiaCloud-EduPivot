package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseTaskVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseTaskService {

    PageInfo<CourseTaskVO> listCourseTask(CourseTaskQueryDTO courseTaskQueryDTO);

    List<CourseTaskVO> listAllCourseTaskByCourseId(UUID courseId);

    List<CourseTaskVO> listAllCourseTaskByUserId(UUID sysUserId);

    List<CourseTaskVO> listCourseTaskByCourseIdAndStatus(UUID courseId, Integer status);

    List<CourseTaskVO> listCourseTaskByCourseIdAndTaskType(UUID courseId, Integer taskType);

    List<CourseTaskVO> listCourseTaskByCourseIdAndDifficulty(UUID courseId, Integer difficulty);

    CourseTaskVO getCourseTaskById(UUID id);

    CourseTaskVO addCourseTask(CourseTaskDTO courseTaskDTO);

    Boolean updateCourseTask(CourseTaskDTO courseTaskDTO);

    Boolean removeCourseTaskById(UUID id);

    Integer removeCourseTaskByIds(List<UUID> ids);

    Boolean updateTaskStatus(UUID id, Integer status);

    Boolean publishTask(UUID id);

    Boolean unpublishTask(UUID id);

    Boolean startTask(UUID id);

    Boolean endTask(UUID id);

    Boolean viewTask(UUID id);

    List<CourseTaskVO> getHotTasks(UUID courseId, Integer limit);

    List<CourseTaskVO> getLatestTasks(UUID courseId, Integer limit);

    List<CourseTaskVO> getTasksByTag(String tag, Integer limit);

    List<CourseTaskVO> searchTasksByName(String taskName, Integer limit);

    java.util.Map<String, Object> getTaskStatistics(UUID courseId);

    java.util.Map<String, Object> getUserTaskStatistics(UUID sysUserId);
}
