package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.system.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.system.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysPermissionMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysPermissionService;
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
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public PageInfo<SysPermissionVO> listSysPermission(SysPermissionQueryDTO sysPermissionQueryDTO) {
        return PageHelper.startPage(sysPermissionQueryDTO.getPageNum(), sysPermissionQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysPermissionMapper.listSysPermission(sysPermissionQueryDTO));
    }

    @Override
    @Cacheable(value = "SysPermission", key = "#p0", condition = "#p0 != null")
    public SysPermission getPermissionById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        SysPermission sysPermission = this.getById(id);
        if (sysPermission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        return sysPermission;
    }

    @Override
    @Transactional
    public Boolean addPermission(SysPermissionAddDTO sysPermissionDTO) {
        if (sysPermissionDTO == null || !StringUtils.hasText(sysPermissionDTO.getPermissionName())) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (!StringUtils.hasText(sysPermissionDTO.getPermissionKey())) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_KEY_CANNOT_BE_EMPTY.getMessage());
        }
        List<SysPermission> exist = this.lambdaQuery().eq(SysPermission::getPermissionKey, sysPermissionDTO.getPermissionKey()).list();
        if (!exist.isEmpty()) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_ALREADY_EXISTS.getMessage());
        }

        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(sysPermissionDTO, sysPermission);

        sysPermission.setId(UuidCreator.getTimeOrderedEpoch());
        sysPermission.setStatus(StatusEnum.NORMAL.getCode());
        sysPermission.setCreateTime(LocalDateTime.now());
        sysPermission.setUpdateTime(LocalDateTime.now());

        return this.save(sysPermission);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysPermission", key = "#p0.id", condition = "#p0.id != null")
    public Boolean updatePermission(SysPermissionDTO sysPermissionDTO) {
        if (sysPermissionDTO == null || sysPermissionDTO.getId() == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        SysPermission sysPermission = this.getById(sysPermissionDTO.getId());
        if (sysPermission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }

        BeanUtils.copyProperties(sysPermissionDTO, sysPermission);
        sysPermission.setUpdateTime(LocalDateTime.now());

        return this.updateById(sysPermission);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysPermission", key = "#p0", condition = "#p0 != null")
    public Boolean removePermissionById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        SysPermission sysPermission = this.getById(id);
        if (sysPermission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        return this.removeById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "SysPermission", allEntries = true)
    public Integer removePermissionByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }

        List<SysPermission> sysPermissions = this.listByIds(ids);
        ids.forEach(id -> {
            if (sysPermissions.stream().noneMatch(permission -> permission.getId().equals(id))) {
                throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
            }
        });

        return this.removeByIds(ids) ? ids.size() : 0;
    }
} 