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

    /**
     * 分页查询学生座位记录列表（关联查询学生、课程信息）
     *
     * @param dto 查询条件
     * @return 学生座位记录列表
     */
    List<CourseRecordStudentVO> listCourseRecordStudent(CourseRecordStudentQueryDTO dto);

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
    List<CourseRecordStudentVO> listStudentsByRecordId(@Param("recordId") UUID recordId);

    /**
     * 查询具体学生座位信息
     *
     * @param recordId  课程记录ID
     * @param studentId 学生ID
     * @return 学生座位信息
     */
    CourseRecordStudentVO getStudentSeat(@Param("recordId") UUID recordId,
                                         @Param("studentId") UUID studentId);

    /**
     * 检查座位是否被占用
     *
     * @param recordId  课程记录ID
     * @param seatIndex 座位编号
     * @return 占用数量（0表示未占用，>0表示已占用）
     */
    Integer checkSeatOccupied(@Param("recordId") UUID recordId,
                              @Param("seatIndex") Integer seatIndex);

    /**
     * 统计课程记录的实到人数
     *
     * @param recordId 课程记录ID
     * @return 实到人数
     */
    Integer countStudentsByRecordId(@Param("recordId") UUID recordId);

    /**
     * 根据课程记录ID列表查询所有学生座位记录
     *
     * @param recordIds 课程记录ID列表
     * @return 学生座位记录列表
     */
    List<CourseRecordStudentVO> listStudentsByRecordIds(@Param("recordIds") List<UUID> recordIds);
}
