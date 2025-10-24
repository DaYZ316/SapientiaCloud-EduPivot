package com.dayz.sapientiacloud_edupivot.classroom.service;

import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.StudentSeatDeleteDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseRecordStudentService {

    /**
     * 分页查询学生座位记录列表
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    PageInfo<CourseRecordStudentVO> listCourseRecordStudentPage(CourseRecordStudentQueryDTO dto);

    /**
     * 查询所有学生座位记录
     *
     * @return 学生座位记录列表
     */
    List<CourseRecordStudentVO> listAllCourseRecordStudent();

    /**
     * 根据课程记录ID查询所有学生座位
     *
     * @param recordId 课程记录ID
     * @return 学生座位记录列表
     */
    List<CourseRecordStudentVO> listStudentsByRecordId(UUID recordId);

    /**
     * 查询具体学生座位信息
     *
     * @param recordId  课程记录ID
     * @param studentId 学生ID
     * @return 学生座位信息
     */
    CourseRecordStudentVO getStudentSeat(UUID recordId, UUID studentId);

    /**
     * 添加学生座位（选座）
     *
     * @param dto 学生座位信息
     * @return 学生座位详情
     */
    CourseRecordStudentVO addStudentSeat(CourseRecordStudentDTO dto);

    /**
     * 更新学生座位信息
     *
     * @param dto 学生座位信息
     * @return 是否成功
     */
    Boolean updateStudentSeat(CourseRecordStudentDTO dto);

    /**
     * 删除学生座位
     *
     * @param recordId  课程记录ID
     * @param studentId 学生ID
     * @return 是否成功
     */
    Boolean removeStudentSeat(UUID recordId, UUID studentId);

    /**
     * 批量删除学生座位
     * 
     * @param dtoList 学生座位删除列表
     * @return 删除的数量
     */
    Integer removeStudentSeatBatch(List<StudentSeatDeleteDTO> dtoList);

    /**
     * 检查座位是否被占用
     *
     * @param recordId  课程记录ID
     * @param seatIndex 座位编号
     * @return 是否被占用
     */
    Boolean checkSeatOccupied(UUID recordId, Integer seatIndex);

    /**
     * 统计课程记录的实到人数
     *
     * @param recordId 课程记录ID
     * @return 实到人数
     */
    Integer countStudentsByRecordId(UUID recordId);
}
