package com.dayz.sapientiacloud_edupivot.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.teacher.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.po.Teacher;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.teacher.enums.TeacherEnum;
import com.dayz.sapientiacloud_edupivot.teacher.mapper.TeacherMapper;
import com.dayz.sapientiacloud_edupivot.teacher.service.ITeacherService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

    private final TeacherMapper teacherMapper;

    @Override
    public PageInfo<TeacherVO> listTeacherPage(TeacherQueryDTO teacherQueryDTO) {
        if (teacherQueryDTO == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND.getMessage());
        }

        PageInfo<TeacherVO> pageInfo = PageHelper.startPage(teacherQueryDTO.getPageNum(), teacherQueryDTO.getPageSize())
                .doSelectPageInfo(() -> teacherMapper.listTeacher(teacherQueryDTO));

        return pageInfo;
    }

    @Override
    public List<TeacherVO> listAllTeacher() {
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Teacher::getCreateTime);

        List<Teacher> teacherList = this.list(queryWrapper);

        return teacherList.stream().map(teacher -> {
            TeacherVO teacherVO = new TeacherVO();
            BeanUtils.copyProperties(teacher, teacherVO);
            return teacherVO;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherVO getTeacherById(UUID id) {
        Teacher teacher = this.getById(id);
        if (teacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND.getMessage());
        }

        TeacherVO teacherVO = new TeacherVO();
        BeanUtils.copyProperties(teacher, teacherVO);

        return teacherVO;
    }

    @Override
    @Transactional(readOnly = true)
    public Teacher getTeacherBySysUserId(UUID sysUserId) {
        return teacherMapper.selectBySysUserId(sysUserId);
    }

    @Override
    @Transactional
    public Boolean addTeacher(TeacherAddDTO teacherAddDTO) {
        if (teacherAddDTO == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND.getMessage());
        }

        if (teacherAddDTO.getSysUserId() != null) {
            Teacher existingTeacher = getTeacherBySysUserId(teacherAddDTO.getSysUserId());
            if (existingTeacher != null) {
                throw new BusinessException(TeacherEnum.SYS_USER_ALREADY_BOUND.getMessage());
            }
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherAddDTO, teacher);
        teacher.setId(UUID.randomUUID());
        teacher.setSysUserId(UserContextUtil.getCurrentUserId());
        teacher.setCreateTime(LocalDateTime.now());
        teacher.setUpdateTime(LocalDateTime.now());

        return this.save(teacher);
    }

    @Override
    @Transactional
    public Boolean updateTeacher(TeacherDTO teacherDTO) {
        if (teacherDTO == null || teacherDTO.getId() == null) {
            throw new BusinessException(TeacherEnum.TEACHER_ID_REQUIRED.getMessage());
        }

        Teacher existingTeacher = this.getById(teacherDTO.getId());
        if (existingTeacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND.getMessage());
        }

        if (teacherDTO.getSysUserId() != null && !teacherDTO.getSysUserId().equals(existingTeacher.getSysUserId())) {
            Teacher boundTeacher = getTeacherBySysUserId(teacherDTO.getSysUserId());
            if (boundTeacher != null && !boundTeacher.getId().equals(teacherDTO.getId())) {
                throw new BusinessException(TeacherEnum.SYS_USER_ALREADY_BOUND.getMessage());
            }
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setUpdateTime(LocalDateTime.now());

        return this.updateById(teacher);
    }

    @Override
    @Transactional
    public Boolean removeTeacherById(UUID id) {
        Teacher teacher = this.getById(id);
        if (teacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND.getMessage());
        }

        return this.removeById(id);
    }

    @Override
    @Transactional
    public Integer removeTeacherByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        return Math.toIntExact(this.removeBatchByIds(ids) ? ids.size() : 0);
    }
}