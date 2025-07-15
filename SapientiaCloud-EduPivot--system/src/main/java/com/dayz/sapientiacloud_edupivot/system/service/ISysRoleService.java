package com.dayz.sapientiacloud_edupivot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysRoleService extends IService<SysRole> {

    PageInfo<SysRoleVO> listSysRole(SysRoleQueryDTO sysRoleQueryDTO);

    SysRoleVO getRoleById(UUID id);

    Boolean addRole(SysRoleAddDTO sysRole);

    Boolean updateRole(SysRoleDTO sysRole);

    Boolean removeRoleById(UUID id);

    Integer removeRoleByIds(List<UUID> ids);

    Boolean assignRolePermissions(UUID id, List<UUID> permissionIds);
}
