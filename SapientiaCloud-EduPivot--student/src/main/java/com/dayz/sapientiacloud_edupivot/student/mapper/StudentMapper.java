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

    List<StudentVO> listStudent(StudentQueryDTO studentQueryDTO);

    Student selectByStudentCode(String studentCode);

    Student selectBySysUserId(UUID sysUserId);
}