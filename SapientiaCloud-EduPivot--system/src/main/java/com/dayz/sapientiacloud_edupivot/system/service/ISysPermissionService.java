package com.dayz.sapientiacloud_edupivot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysPermissionService extends IService<SysPermission> {

    PageInfo<SysPermissionVO> listSysPermission(SysPermissionQueryDTO sysPermissionQueryDTO);

    SysPermission getPermissionById(UUID id);

    Boolean addPermission(SysPermissionAddDTO sysPermissionDTO);

    Boolean updatePermission(SysPermissionDTO sysPermissionDTO);

    Boolean removePermissionById(UUID id);

    Integer removePermissionByIds(List<UUID> ids);
} 