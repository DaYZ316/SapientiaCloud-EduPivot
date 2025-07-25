package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.common.security.service.PermissionService;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysPermissionMapper;
import com.dayz.sapientiacloud_edupivot.system.service.ISysPermissionService;
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
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final PermissionService permissionService;

    @Override
    public PageInfo<SysPermissionVO> listSysPermission(SysPermissionQueryDTO sysPermissionQueryDTO) {
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(sysPermissionQueryDTO.getPermissionName())) {
            queryWrapper.like(SysPermission::getPermissionName, sysPermissionQueryDTO.getPermissionName());
        }
        
        if (StringUtils.hasText(sysPermissionQueryDTO.getPermissionKey())) {
            queryWrapper.like(SysPermission::getPermissionKey, sysPermissionQueryDTO.getPermissionKey());
        }
        
        // 排序
        queryWrapper.orderByAsc(SysPermission::getSort).orderByAsc(SysPermission::getId);
        
        // 查询
        List<SysPermission> sysPermissions = sysPermissionMapper.selectList(queryWrapper);
        
        // 转换为VO
        List<SysPermissionVO> sysPermissionVOS = sysPermissions.stream()
                .map(this::convert)
                .toList();
        
        return new PageInfo<>(sysPermissionVOS);
    }

    @Override
    public SysPermission getPermissionById(UUID id) {
        return sysPermissionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addPermission(SysPermissionAddDTO sysPermissionDTO) {
        // 检查权限标识是否已存在
        LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysPermission::getPermissionKey, sysPermissionDTO.getPermissionKey());
        if (sysPermissionMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_KEY_EXISTS.getMessage());
        }
        
        // 创建权限
        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(sysPermissionDTO, sysPermission);
        
        // 保存权限
        int result = sysPermissionMapper.insert(sysPermission);
        
        // 清除权限缓存
        permissionService.clearAllPermissionCache();
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePermission(SysPermissionDTO sysPermissionDTO) {
        // 检查权限是否存在
        SysPermission existingPermission = getPermissionById(sysPermissionDTO.getId());
        if (existingPermission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        
        // 检查权限标识是否重复
        if (!existingPermission.getPermissionKey().equals(sysPermissionDTO.getPermissionKey())) {
            LambdaQueryWrapper<SysPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysPermission::getPermissionKey, sysPermissionDTO.getPermissionKey());
            if (sysPermissionMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException(SysPermissionEnum.PERMISSION_KEY_EXISTS.getMessage());
            }
        }
        
        // 更新权限
        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(sysPermissionDTO, sysPermission);
        
        // 保存更新
        int result = sysPermissionMapper.updateById(sysPermission);
        
        // 清除权限缓存
        permissionService.clearAllPermissionCache();
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removePermissionById(UUID id) {
        // 检查权限是否存在
        SysPermission permission = getPermissionById(id);
        if (permission == null) {
            throw new BusinessException(SysPermissionEnum.PERMISSION_NOT_FOUND.getMessage());
        }
        
        // 删除权限
        int result = sysPermissionMapper.deleteById(id);
        
        // 清除权限缓存
        permissionService.clearAllPermissionCache();
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer removePermissionByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        // 删除权限
        int result = sysPermissionMapper.deleteBatchIds(ids);
        
        // 清除权限缓存
        permissionService.clearAllPermissionCache();
        
        return result;
    }
    
    /**
     * 将SysPermission转换为SysPermissionVO
     */
    private SysPermissionVO convert(SysPermission sysPermission) {
        if (sysPermission == null) {
            return null;
        }
        
        SysPermissionVO sysPermissionVO = new SysPermissionVO();
        BeanUtils.copyProperties(sysPermission, sysPermissionVO);
        return sysPermissionVO;
    }
} 