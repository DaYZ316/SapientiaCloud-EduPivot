package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseThread;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseThreadVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseThreadMapper extends BaseMapper<CourseThread> {

    List<CourseThreadVO> listCourseThread(CourseThreadQueryDTO courseThreadQueryDTO);

    List<CourseThreadVO> listAllCourseThread();

    CourseThreadVO getCourseThreadById(UUID threadId);

    Boolean addCourseThread(CourseThread courseThread);

    Boolean updateCourseThread(CourseThread courseThread);

    Boolean removeCourseThreadById(UUID threadId);

    Integer removeCourseThreadByIds(List<UUID> threadIds);

    Boolean pinThread(UUID id, Boolean pinned);

    Boolean closeThread(UUID id, Boolean closed);

    Boolean viewThread(UUID id);
}
