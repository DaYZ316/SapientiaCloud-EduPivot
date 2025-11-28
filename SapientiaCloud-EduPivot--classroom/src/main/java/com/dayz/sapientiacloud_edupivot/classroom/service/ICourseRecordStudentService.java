package com.dayz.sapientiacloud_edupivot.classroom.service;

import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.StudentSeatDeleteDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseRecordStudentService {

    PageInfo<CourseRecordStudentVO> listCourseRecordStudentPage(CourseRecordStudentQueryDTO courseRecordStudentQueryDTO);

    List<CourseRecordStudentVO> listAllCourseRecordStudent();

    List<CourseRecordStudentVO> listStudentsByRecordId(UUID recordId);

    CourseRecordStudentVO getStudentSeat(UUID recordId, UUID studentId);

    CourseRecordStudentVO addStudentSeat(CourseRecordStudentDTO courseRecordStudentDTO);

    Boolean updateStudentSeat(CourseRecordStudentDTO courseRecordStudentDTO);

    Boolean removeStudentSeat(UUID recordId, UUID studentId);

    Integer removeStudentSeatBatch(List<StudentSeatDeleteDTO> dtoList);

    Boolean checkSeatOccupied(UUID recordId, Integer seatIndex);

    Integer countStudentsByRecordId(UUID recordId);
}
