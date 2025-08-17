package com.dayz.sapientiacloud_edupivot.student.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.Student;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IStudentService extends IService<Student> {

    PageInfo<StudentVO> listStudentPage(StudentQueryDTO studentQueryDTO);

    List<StudentVO> listAllStudent();

    StudentVO getStudentById(UUID id);

    Student getStudentByCode(String studentCode);

    Student getStudentBySysUserId(UUID sysUserId);

    Boolean addStudent(StudentAddDTO studentAddDTO);

    Boolean updateStudent(StudentDTO studentDTO);

    Boolean removeStudentById(UUID id);

    Integer removeStudentByIds(List<UUID> ids);

    Boolean checkStudentCodeExists(String studentCode, UUID excludeId);
}