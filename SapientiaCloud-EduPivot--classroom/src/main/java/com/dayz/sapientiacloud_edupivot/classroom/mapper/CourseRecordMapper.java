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

    /**
     * 分页查询课程记录列表（关联查询教师、课程信息）
     *
     * @param dto 查询条件
     * @return 课程记录列表
     */
    List<CourseRecordVO> listCourseRecord(CourseRecordQueryDTO dto);

    /**
     * 查询所有课程记录
     *
     * @return 课程记录列表
     */
    List<CourseRecordVO> listAllCourseRecord();

    /**
     * 根据ID查询课程记录详情（关联查询）
     *
     * @param id 课程记录ID
     * @return 课程记录详情
     */
    CourseRecordVO getCourseRecordById(@Param("id") UUID id);

    /**
     * 根据课程ID查询课程记录列表
     *
     * @param courseId 课程ID
     * @return 课程记录列表
     */
    List<CourseRecordVO> listCourseRecordByCourseId(@Param("courseId") UUID courseId);

    /**
     * 根据教师ID查询课程记录列表
     *
     * @param teacherId 教师ID
     * @return 课程记录列表
     */
    List<CourseRecordVO> listCourseRecordByTeacherId(@Param("teacherId") UUID teacherId);
}
