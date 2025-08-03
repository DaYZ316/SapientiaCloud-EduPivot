package com.dayz.sapientiacloud_edupivot.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.Student;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IStudentService extends IService<Student> {

    /**
     * 分页查询学生列表
     */
    PageInfo<StudentVO> listStudentPage(StudentQueryDTO studentQueryDTO);

    /**
     * 查询所有学生
     */
    List<StudentVO> listAllStudent();

    /**
     * 根据ID获取学生信息
     */
    StudentVO getStudentById(UUID id);

    /**
     * 根据学号查询学生
     */
    Student getStudentByCode(String studentCode);

    /**
     * 根据系统用户ID查询学生
     */
    Student getStudentBySysUserId(UUID sysUserId);

    /**
     * 添加学生
     */
    Boolean addStudent(StudentDTO studentDTO);

    /**
     * 更新学生信息
     */
    Boolean updateStudent(StudentDTO studentDTO);

    /**
     * 根据ID删除学生
     */
    Boolean removeStudentById(UUID id);

    /**
     * 批量删除学生
     */
    Integer removeStudentByIds(List<UUID> ids);

    /**
     * 检查学号是否存在
     */
    Boolean checkStudentCodeExists(String studentCode, UUID excludeId);
}