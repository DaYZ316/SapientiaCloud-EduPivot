package com.dayz.sapientiacloud_edupivot.teacher.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.teacher.common.clients.SysRoleClient;
import com.dayz.sapientiacloud_edupivot.teacher.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.teacher.common.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import com.dayz.sapientiacloud_edupivot.teacher.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.po.Teacher;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.teacher.enums.TeacherEnum;
import com.dayz.sapientiacloud_edupivot.teacher.mapper.TeacherMapper;
import com.dayz.sapientiacloud_edupivot.teacher.service.ITeacherService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements ITeacherService {

    private static final String TEACHER = "TEACHER";

    private final TeacherMapper teacherMapper;
    private final SysRoleClient sysRoleClient;
    private final SysUserClient sysUserClient;

    @Override
    public PageInfo<TeacherVO> listTeacher(TeacherQueryDTO teacherQueryDTO) {
        if (teacherQueryDTO == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        return PageHelper.startPage(teacherQueryDTO.getPageNum(), teacherQueryDTO.getPageSize())
                .doSelectPageInfo(() -> teacherMapper.listTeacher(teacherQueryDTO));
    }

    @Override
    @Cacheable(value = "Teacher", key = "'all'", condition = "true")
    public List<TeacherVO> listAllTeacher() {
        return teacherMapper.listAllTeacherWithUserInfo();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Teacher", key = "#p0", condition = "#p0 != null")
    public TeacherVO getTeacherById(UUID id) {
        if (id == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        Teacher teacher = this.getById(id);
        if (teacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        TeacherVO teacherVO = new TeacherVO();
        BeanUtils.copyProperties(teacher, teacherVO);

        if (teacher.getSysUserId() != null) {
            Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoById(teacher.getSysUserId());
            if (userResult.isSuccess() && userResult.getData() != null) {
                SysUserInternalVO userInfo = userResult.getData();
                teacherVO.setAvatar(userInfo.getAvatar());
                teacherVO.setUsername(userInfo.getUsername());
                teacherVO.setNickName(userInfo.getNickName());
                teacherVO.setEmail(userInfo.getEmail());
                teacherVO.setMobile(userInfo.getMobile());
                teacherVO.setGender(userInfo.getGender());
                teacherVO.setStatus(userInfo.getStatus());
                teacherVO.setLastLoginTime(userInfo.getLastLoginTime());
            }
        }

        return teacherVO;
    }

    @Override
    @Transactional(readOnly = true)
    // TODO Cache
    public TeacherVO getTeacherByUserId(UUID sysUserId) {
        if (sysUserId == null) {
            return null;
        }

        Teacher teacher = teacherMapper.selectByUserId(sysUserId);
        if (teacher == null) {
            return null;
        }

        TeacherVO teacherVO = new TeacherVO();
        BeanUtils.copyProperties(teacher, teacherVO);

        return teacherVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "Teacher", allEntries = true)
    public Boolean addTeacher(TeacherAddDTO teacherAddDTO) {
        if (teacherAddDTO == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        if (teacherAddDTO.getSysUserId() != null) {
            TeacherVO existingTeacher = getTeacherByUserId(teacherAddDTO.getSysUserId());
            if (existingTeacher != null) {
                throw new BusinessException(TeacherEnum.SYS_USER_ALREADY_BOUND);
            }
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherAddDTO, teacher);
        teacher.setId(UuidCreator.getTimeOrderedEpoch());
        teacher.setSysUserId(UserContextUtil.getCurrentUserId());
        if (sysRoleClient.getRoleByKey(TEACHER).getData() != null) {
            sysRoleClient.addRoleToUser(teacher.getSysUserId(), TEACHER);
        }

        teacher.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        teacher.setCreateTime(LocalDateTime.now());
        teacher.setUpdateTime(LocalDateTime.now());

        return this.save(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Teacher", key = "#p0.id", condition = "#p0.id != null"),
            @CacheEvict(value = "Teacher", key = "'all'", condition = "true")
    })
    public Boolean updateTeacher(TeacherDTO teacherDTO) {
        if (teacherDTO == null || teacherDTO.getId() == null) {
            throw new BusinessException(TeacherEnum.TEACHER_ID_REQUIRED);
        }

        Teacher existingTeacher = this.getById(teacherDTO.getId());
        if (existingTeacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        if (teacherDTO.getSysUserId() != null && !teacherDTO.getSysUserId().equals(existingTeacher.getSysUserId())) {
            TeacherVO boundTeacher = getTeacherByUserId(teacherDTO.getSysUserId());
            if (boundTeacher != null && !boundTeacher.getId().equals(teacherDTO.getId())) {
                throw new BusinessException(TeacherEnum.SYS_USER_ALREADY_BOUND);
            }
        }

        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(teacherDTO, teacher);
        teacher.setUpdateTime(LocalDateTime.now());

        return this.updateById(teacher);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "Teacher", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "Teacher", key = "'all'", condition = "true")
    })
    public Boolean removeTeacherById(UUID id) {
        if (id == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        Teacher teacher = this.getById(id);
        if (teacher == null) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        boolean removeResult = this.removeById(id);
        if (removeResult) {
            // 同步删除用户的教师角色绑定
            sysRoleClient.removeRoleFromUser(teacher.getSysUserId(), TEACHER);
        }

        return removeResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "Teacher", allEntries = true)
    public Integer removeTeacherByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
        }

        List<Teacher> teachers = this.listByIds(ids);
        ids.forEach(id -> {
            if (teachers.stream().noneMatch(teacher -> teacher.getId().equals(id))) {
                throw new BusinessException(TeacherEnum.TEACHER_NOT_FOUND);
            }
        });

        boolean removeResult = this.removeBatchByIds(ids);
        if (removeResult) {
            // 同步删除用户的教师角色绑定
            for (Teacher teacher : teachers) {
                sysRoleClient.removeRoleFromUser(teacher.getSysUserId(), TEACHER);
            }
        }

        return Math.toIntExact(removeResult ? ids.size() : 0);
    }
}