package com.dayz.sapientiacloud_edupivot.classroom.service;

import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseRecordService {

    /**
     * 分页查询课程记录列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageInfo<CourseRecordVO> listCourseRecordPage(CourseRecordQueryDTO dto);

    /**
     * 查询所有课程记录
     *
     * @return 课程记录列表
     */
    List<CourseRecordVO> listAllCourseRecord();

    /**
     * 根据ID查询课程记录详情
     *
     * @param id 课程记录ID
     * @return 课程记录详情
     */
    CourseRecordVO getCourseRecordById(UUID id);

    /**
     * 添加课程记录（开课）
     *
     * @param dto 课程记录信息
     * @return 课程记录详情
     */
    CourseRecordVO addCourseRecord(CourseRecordDTO dto);

    /**
     * 更新课程记录
     *
     * @param dto 课程记录信息
     * @return 是否成功
     */
    Boolean updateCourseRecord(CourseRecordDTO dto);

    /**
     * 根据ID删除课程记录
     *
     * @param id 课程记录ID
     * @return 是否成功
     */
    Boolean removeCourseRecordById(UUID id);

    /**
     * 批量删除课程记录
     *
     * @param ids 课程记录ID列表
     * @return 删除数量
     */
    Integer removeCourseRecordByIds(List<UUID> ids);

    /**
     * 根据课程ID查询课程记录列表
     *
     * @param courseId 课程ID
     * @return 课程记录列表
     */
    List<CourseRecordVO> listCourseRecordByCourseId(UUID courseId);

    /**
     * 根据教师ID查询课程记录列表
     *
     * @param teacherId 教师ID
     * @return 课程记录列表
     */
    List<CourseRecordVO> listCourseRecordByTeacherId(UUID teacherId);

    /**
     * 结束课程记录
     *
     * @param id 课程记录ID
     * @return 是否成功
     */
    Boolean endCourseRecord(UUID id);
}
