package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.system.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysRoleService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public PageInfo<SysRoleVO> listSysRole(SysRoleQueryDTO sysRoleQueryDTO) {
        return PageHelper.startPage(sysRoleQueryDTO.getPageNum(), sysRoleQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysRoleMapper.listSysRole(sysRoleQueryDTO));
    }

    @Override
    @Cacheable(value = "SysRole", key = "#p0", condition = "#p0 != null")
    public SysRole getRoleById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        SysRole sysRole = this.getById(id);
        if (sysRole == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        return sysRole;
    }

    @Override
    @Transactional
    public Boolean addRole(SysRoleAddDTO sysRoleDTO) {
        if (sysRoleDTO == null || !StringUtils.hasText(sysRoleDTO.getRoleName())) {
            throw new BusinessException(SysRoleEnum.ROLE_NAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (!StringUtils.hasText(sysRoleDTO.getRoleKey())) {
            throw new BusinessException(SysRoleEnum.ROLE_KEY_CANNOT_BE_EMPTY.getMessage());
        }
        List<SysRole> exist = this.lambdaQuery().eq(SysRole::getRoleKey, sysRoleDTO.getRoleKey()).list();
        if (!exist.isEmpty()) {
            throw new BusinessException(SysRoleEnum.ROLE_ALREADY_EXISTS.getMessage());
        }

        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(sysRoleDTO, sysRole);

        sysRole.setId(UuidCreator.getTimeOrderedEpoch());
        sysRole.setStatus(StatusEnum.NORMAL.getCode());
        sysRole.setCreateTime(LocalDateTime.now());
        sysRole.setUpdateTime(LocalDateTime.now());
        return this.save(sysRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysRole", key = "#p0.id", condition = "#p0.id != null")
    public Boolean updateRole(SysRoleDTO sysRoleDTO) {
        if (sysRoleDTO == null || sysRoleDTO.getId() == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        if (sysRoleDTO.isAdmin()) {
            throw new BusinessException(SysRoleEnum.ADMIN_OPERATION_FORBIDDEN.getMessage());
        }
        SysRole sysRole = this.getById(sysRoleDTO.getId());
        if (sysRole == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        BeanUtils.copyProperties(sysRoleDTO, sysRole);
        sysRole.setUpdateTime(LocalDateTime.now());
        return this.updateById(sysRole);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysRole", key = "#p0", condition = "#p0 != null")
    public Boolean removeRoleById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        SysRole sysRole = this.getById(id);
        if (sysRole == null) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }
        if (sysRole.isAdmin()) {
            throw new BusinessException(SysRoleEnum.ADMIN_OPERATION_FORBIDDEN.getMessage());
        }
        return this.removeById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysRole", allEntries = true)
    public Integer removeRoleByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
        }

        List<SysRole> sysRoles = this.listByIds(ids);
        ids.forEach(id -> {
            if (sysRoles.stream().noneMatch(role -> role.getId().equals(id))) {
                throw new BusinessException(SysRoleEnum.ROLE_NOT_FOUND.getMessage());
            }
            if (sysRoles.stream().anyMatch(SysRole::isAdmin)) {
                throw new BusinessException(SysRoleEnum.ADMIN_OPERATION_FORBIDDEN.getMessage());
            }

        });

        return this.removeByIds(ids) ? ids.size() : 0;
    }
}
