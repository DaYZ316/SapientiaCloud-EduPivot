package com.dayz.sapientiacloud_edupivot.classroom.service;

import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ICourseRecordService {

    PageInfo<CourseRecordVO> listCourseRecordPage(CourseRecordQueryDTO dto);

    List<CourseRecordVO> listAllCourseRecord();

    CourseRecordVO getCourseRecordById(UUID id);

    CourseRecordVO addCourseRecord(CourseRecordDTO dto);

    Boolean updateCourseRecord(CourseRecordDTO dto);

    Boolean removeCourseRecordById(UUID id);

    Integer removeCourseRecordByIds(List<UUID> ids);

    List<CourseRecordVO> listCourseRecordByCourseId(UUID courseId);

    List<CourseRecordVO> listCourseRecordByTeacherId(UUID teacherId);

    Boolean endCourseRecord(UUID id);

    LocalDateTime getCourseEndTimeById(UUID id);
}
