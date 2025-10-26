package com.dayz.sapientiacloud_edupivot.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.CourseRecordStudent;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseRecordStudentMapper extends BaseMapper<CourseRecordStudent> {

    List<CourseRecordStudentVO> listCourseRecordStudent(CourseRecordStudentQueryDTO dto);

    List<CourseRecordStudentVO> listAllCourseRecordStudent();

    List<CourseRecordStudentVO> listStudentsByRecordId(@Param("recordId") UUID recordId);

    CourseRecordStudentVO getStudentSeat(@Param("recordId") UUID recordId, @Param("studentId") UUID studentId);

    Integer checkSeatOccupied(@Param("recordId") UUID recordId, @Param("seatIndex") Integer seatIndex);

    Integer countStudentsByRecordId(@Param("recordId") UUID recordId);

    List<CourseRecordStudentVO> listStudentsByRecordIds(@Param("recordIds") List<UUID> recordIds);
}
