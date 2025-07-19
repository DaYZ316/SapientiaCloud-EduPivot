package com.dayz.sapientiacloud_edupivot.system.service;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysUserService {
    PageInfo<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO);

    SysUserVO getUserById(UUID id);

    Boolean updateUser(SysUserDTO sysUserDTO);

    Boolean removeUserById(UUID id);

    Integer removeUserByIds(List<UUID> ids);

    Boolean assignRoles(UUID userId, List<UUID> roleIds);

    List<SysRoleVO> getUserRoles(UUID userId);

    SysUserVO addUser(SysUserAdminDTO sysUserAdminDTO);

    SysUserInternalDTO selectUserByUsername(String username);

    List<SysPermissionVO> getUserPermissions(UUID userId);

    Boolean registerUser(SysUserRegisterDTO sysUserRegisterDTO);
}
