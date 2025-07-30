package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysPermissionMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysRolePermissionMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysPermissionService;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public PageInfo<SysPermissionVO> listSysPermission(SysPermissionQueryDTO sysPermissionQueryDTO) {
        if (sysPermissionQueryDTO == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }

        return PageHelper.startPage(sysPermissionQueryDTO.getPageNum(), sysPermissionQueryDTO.getPageSize())
                .doSelectPageInfo(() -> sysPermissionMapper.listSysPermission(sysPermissionQueryDTO));
    }

    @Override
    public List<SysPermissionVO> listSysPermissionTree() {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(SysPermission::getSort);

        List<SysPermission> sysPermissionList = this.list(queryWrapper);

        List<SysPermissionVO> sysPermissionVOList = sysPermissionList.stream().map(sysPermission -> {
            SysPermissionVO sysPermissionVO = new SysPermissionVO();
            BeanUtils.copyProperties(sysPermission, sysPermissionVO);

            return sysPermissionVO;
        }).toList();

        return buildPermissionTree(sysPermissionVOList);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(rollbackFor = Exception.class)
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
        sysPermission.setCreateTime(LocalDateTime.now());
        sysPermission.setUpdateTime(LocalDateTime.now());

        return this.save(sysPermission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysPermission", key = "#p0", condition = "#p0 != null"),
            @CacheEvict(value = "SysRole", allEntries = true)
    })
    public Boolean removePermissionById(UUID id) {
        if (id == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }

        SysPermission sysPermission = this.getById(id);
        if (sysPermission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }

        boolean removed = this.removeById(id);
        if (Boolean.TRUE.equals(removed)) {
            sysRolePermissionMapper.removePermissionsById(id);
            sysPermissionMapper.removeChildrenById(id);
        }
        return removed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "SysPermission", allEntries = true),
            @CacheEvict(value = "SysRole", allEntries = true)
    })
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

        int count = this.removeByIds(ids) ? ids.size() : 0;
        if (count > 0) {
            sysRolePermissionMapper.removePermissionsByIds(ids);
            sysPermissionMapper.removeChildrenByIds(ids);
        }

        return count;
    }

    private List<SysPermissionVO> buildPermissionTree(List<SysPermissionVO> sysPermissionVOList) {
        List<SysPermissionVO> rootNodes = new ArrayList<>();

        Map<UUID, SysPermissionVO> permissionMap = sysPermissionVOList.stream()
                .collect(Collectors.toMap(SysPermissionVO::getId, sysPermissionVO -> sysPermissionVO));

        for (SysPermissionVO sysPermissionVO : sysPermissionVOList) {
            if (sysPermissionVO.getParentId() == null || !permissionMap.containsKey(sysPermissionVO.getParentId())) {
                rootNodes.add(sysPermissionVO);
            } else {
                SysPermissionVO parent = permissionMap.get(sysPermissionVO.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }

                    parent.getChildren().add(sysPermissionVO);
                }
            }
        }
        return rootNodes;
    }
} 