package com.dayz.sapientiacloud_edupivot.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.CourseRecord;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseRecordMapper extends BaseMapper<CourseRecord> {

    List<CourseRecordVO> listCourseRecord(CourseRecordQueryDTO dto);

    List<CourseRecordVO> listAllCourseRecord();

    CourseRecordVO getCourseRecordById(@Param("id") UUID id);

    List<CourseRecordVO> listCourseRecordByCourseId(@Param("courseId") UUID courseId);

    List<CourseRecordVO> listCourseRecordByTeacherId(@Param("teacherId") UUID teacherId);
}
