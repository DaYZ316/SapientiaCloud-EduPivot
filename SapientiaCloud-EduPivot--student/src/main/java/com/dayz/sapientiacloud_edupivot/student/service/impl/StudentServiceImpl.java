package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    private final StudentMapper studentMapper;

    @Override
    public PageInfo<StudentVO> listStudentPage(StudentQueryDTO studentQueryDTO) {
        if (studentQueryDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND.getMessage());
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
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND.getMessage());
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        return studentVO;
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentByCode(String studentCode) {
        if (!StringUtils.hasText(studentCode)) {
            throw new BusinessException(StudentEnum.STUDENT_CODE_REQUIRED.getMessage());
        }

        return studentMapper.selectByStudentCode(studentCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentBySysUserId(UUID sysUserId) {
        return studentMapper.selectBySysUserId(sysUserId);
    }

    @Override
    @Transactional
    public Boolean addStudent(StudentDTO studentDTO) {
        if (studentDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND.getMessage());
        }

        if (checkStudentCodeExists(studentDTO.getStudentCode(), null)) {
            throw new BusinessException(StudentEnum.STUDENT_CODE_EXISTS.getMessage());
        }

        if (studentDTO.getSysUserId() != null) {
            Student existingStudent = getStudentBySysUserId(studentDTO.getSysUserId());
            if (existingStudent != null) {
                throw new BusinessException(StudentEnum.SYS_USER_ALREADY_BOUND.getMessage());
            }
        }

        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);
        student.setId(UUID.randomUUID());

        return this.save(student);
    }

    @Override
    @Transactional
    public Boolean updateStudent(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getId() == null) {
            throw new BusinessException(StudentEnum.STUDENT_ID_REQUIRED.getMessage());
        }

        Student existingStudent = this.getById(studentDTO.getId());
        if (existingStudent == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND.getMessage());
        }

        if (checkStudentCodeExists(studentDTO.getStudentCode(), studentDTO.getId())) {
            throw new BusinessException(StudentEnum.STUDENT_CODE_EXISTS.getMessage());
        }

        if (studentDTO.getSysUserId() != null && !studentDTO.getSysUserId().equals(existingStudent.getSysUserId())) {
            Student boundStudent = getStudentBySysUserId(studentDTO.getSysUserId());
            if (boundStudent != null && !boundStudent.getId().equals(studentDTO.getId())) {
                throw new BusinessException(StudentEnum.SYS_USER_ALREADY_BOUND.getMessage());
            }
        }

        Student student = new Student();
        BeanUtils.copyProperties(studentDTO, student);

        return this.updateById(student);
    }

    @Override
    @Transactional
    public Boolean removeStudentById(UUID id) {
        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND.getMessage());
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

    @Override
    @Transactional(readOnly = true)
    public Boolean checkStudentCodeExists(String studentCode, UUID excludeId) {
        if (!StringUtils.hasText(studentCode)) {
            return false;
        }

        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentCode, studentCode);
        if (excludeId != null) {
            queryWrapper.ne(Student::getId, excludeId);
        }

        return this.count(queryWrapper) > 0;
    }
}