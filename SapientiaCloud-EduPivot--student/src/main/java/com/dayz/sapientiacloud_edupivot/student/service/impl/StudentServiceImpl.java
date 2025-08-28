package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.Student;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.enums.StudentEnum;
import com.dayz.sapientiacloud_edupivot.student.mapper.StudentMapper;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    private final StudentMapper studentMapper;

    @Override
    public PageInfo<StudentVO> listStudentPage(StudentQueryDTO studentQueryDTO) {
        if (studentQueryDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        PageInfo<StudentVO> pageInfo = PageHelper.startPage(studentQueryDTO.getPageNum(), studentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> studentMapper.listStudent(studentQueryDTO));

        return pageInfo;
    }

    @Override
    public List<StudentVO> listAllStudent() {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Student::getCreateTime);

        List<Student> studentList = this.list(queryWrapper);

        return studentList.stream().map(student -> {
            StudentVO studentVO = new StudentVO();
            BeanUtils.copyProperties(student, studentVO);
            return studentVO;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentVO getStudentById(UUID id) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        return studentVO;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentVO getStudentBySysUserId(UUID sysUserId) {
        Student student = studentMapper.selectBySysUserId(sysUserId);
        if (student == null) {
            return null;
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        return studentVO;
    }

    @Override
    @Transactional
    public Boolean addStudent(StudentAddDTO studentAddDTO) {
        if (studentAddDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        if (studentAddDTO.getSysUserId() != null) {
            StudentVO existingStudent = getStudentBySysUserId(studentAddDTO.getSysUserId());
            if (existingStudent != null) {
                throw new BusinessException(StudentEnum.SYS_USER_ALREADY_BOUND);
            }
        }

        Student student = new Student();
        BeanUtils.copyProperties(studentAddDTO, student);
        student.setId(UUID.randomUUID());
        student.setSysUserId(UserContextUtil.getCurrentUserId());
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());

        return this.save(student);
    }

    @Override
    @Transactional
    public Boolean updateStudent(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getId() == null) {
            throw new BusinessException(StudentEnum.STUDENT_ID_REQUIRED);
        }

        Student existingStudent = this.getById(studentDTO.getId());
        if (existingStudent == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        if (studentDTO.getSysUserId() != null && !studentDTO.getSysUserId().equals(existingStudent.getSysUserId())) {
            StudentVO boundStudent = getStudentBySysUserId(studentDTO.getSysUserId());
            if (boundStudent != null && !boundStudent.getId().equals(studentDTO.getId())) {
                throw new BusinessException(StudentEnum.SYS_USER_ALREADY_BOUND);
            }
        }

        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        student.setUpdateTime(LocalDateTime.now());

        return this.updateById(student);
    }

    @Override
    @Transactional
    public Boolean removeStudentById(UUID id) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        return this.removeById(id);
    }

    @Override
    @Transactional
    public Integer removeStudentByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        return Math.toIntExact(this.removeBatchByIds(ids) ? ids.size() : 0);
    }
}