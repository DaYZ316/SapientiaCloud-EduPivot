package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.student.common.clients.SysRoleClient;
import com.dayz.sapientiacloud_edupivot.student.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.student.common.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.student.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.Student;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.enums.StudentEnum;
import com.dayz.sapientiacloud_edupivot.student.mapper.StudentMapper;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    private static final String STUDENT = "STUDENT";

    private final StudentMapper studentMapper;
    private final SysRoleClient sysRoleClient;
    private final SysUserClient sysUserClient;

    @Override
    public PageInfo<StudentVO> listStudent(StudentQueryDTO studentQueryDTO) {
        if (studentQueryDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        return PageHelper.startPage(studentQueryDTO.getPageNum(), studentQueryDTO.getPageSize())
                .doSelectPageInfo(() -> studentMapper.listStudent(studentQueryDTO));
    }

    @Override
    @Cacheable(value = "Student", key = "'all'", condition = "true")
    public List<StudentVO> listAllStudent() {
        return studentMapper.listAllStudentWithUserInfo();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Student", key = "#p0", condition = "#p0 != null")
    public StudentVO getStudentById(UUID id) {
        if (id == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        // 获取用户信息（除了密码）
        if (student.getSysUserId() != null) {
            try {
                Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoById(student.getSysUserId());
                if (userResult.isSuccess() && userResult.getData() != null) {
                    SysUserInternalVO userInfo = userResult.getData();
                    studentVO.setAvatar(userInfo.getAvatar());
                    studentVO.setUsername(userInfo.getUsername());
                    studentVO.setNickName(userInfo.getNickName());
                    studentVO.setEmail(userInfo.getEmail());
                    studentVO.setMobile(userInfo.getMobile());
                    studentVO.setGender(userInfo.getGender());
                    studentVO.setStatus(userInfo.getStatus());
                    studentVO.setLastLoginTime(userInfo.getLastLoginTime());
                }
            } catch (Exception e) {
                log.warn("获取学生用户信息失败: studentId={}, sysUserId={}", student.getId(), student.getSysUserId(), e);
            }
        }

        return studentVO;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentVO getStudentByUserId(UUID sysUserId) {
        if (sysUserId == null) {
            return null;
        }

        Student student = studentMapper.selectByUserId(sysUserId);
        if (student == null) {
            return null;
        }

        StudentVO studentVO = new StudentVO();
        BeanUtils.copyProperties(student, studentVO);

        return studentVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "Student", allEntries = true)
    public Boolean addStudent(StudentAddDTO studentAddDTO) {
        if (studentAddDTO == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        if (studentAddDTO.getSysUserId() != null) {
            StudentVO existingStudent = getStudentByUserId(studentAddDTO.getSysUserId());
            if (existingStudent != null) {
                throw new BusinessException(StudentEnum.SYS_USER_ALREADY_BOUND);
            }
        }

        Student student = new Student();
        BeanUtils.copyProperties(studentAddDTO, student);
        student.setId(UuidCreator.getTimeOrderedEpoch());
        student.setSysUserId(UserContextUtil.getCurrentUserId());
        if (sysRoleClient.getRoleByKey(STUDENT).getData() != null) {
            sysRoleClient.addRoleToUser(student.getSysUserId(), STUDENT);
        }

        student.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());

        return this.save(student);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Student", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "Student", key = "'all'", condition = "true")
    })
    public Boolean updateStudent(StudentDTO studentDTO) {
        if (studentDTO == null || studentDTO.getId() == null) {
            throw new BusinessException(StudentEnum.STUDENT_ID_REQUIRED);
        }

        Student existingStudent = this.getById(studentDTO.getId());
        if (existingStudent == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        if (studentDTO.getSysUserId() != null && !studentDTO.getSysUserId().equals(existingStudent.getSysUserId())) {
            StudentVO boundStudent = getStudentByUserId(studentDTO.getSysUserId());
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
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Student", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "Student", key = "'all'", condition = "true")
    })
    public Boolean removeStudentById(UUID id) {
        if (id == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        Student student = this.getById(id);
        if (student == null) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        boolean removeResult = this.removeById(id);
        if (removeResult) {
            // 同步删除用户的学生角色绑定
            sysRoleClient.removeRoleFromUser(student.getSysUserId(), STUDENT);
        }

        return removeResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "Student", allEntries = true)
    public Integer removeStudentByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
        }

        List<Student> students = this.listByIds(ids);
        ids.forEach(id -> {
            if (students.stream().noneMatch(student -> student.getId().equals(id))) {
                throw new BusinessException(StudentEnum.STUDENT_NOT_FOUND);
            }
        });

        boolean removeResult = this.removeBatchByIds(ids);
        if (removeResult) {
            // 同步删除用户的学生角色绑定
            for (Student student : students) {
                sysRoleClient.removeRoleFromUser(student.getSysUserId(), STUDENT);
            }
        }

        return Math.toIntExact(removeResult ? ids.size() : 0);
    }
}