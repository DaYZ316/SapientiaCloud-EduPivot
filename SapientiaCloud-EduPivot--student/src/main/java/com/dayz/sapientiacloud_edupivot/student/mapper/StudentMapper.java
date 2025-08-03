package com.dayz.sapientiacloud_edupivot.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.Student;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 分页查询学生列表
     */
    List<StudentVO> listStudent(StudentQueryDTO studentQueryDTO);

    /**
     * 根据学号查询学生
     */
    Student selectByStudentCode(String studentCode);

    /**
     * 根据系统用户ID查询学生
     */
    Student selectBySysUserId(UUID sysUserId);
}