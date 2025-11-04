package com.dayz.sapientiacloud_edupivot.system.service;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.*;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysUserService {

    PageInfo<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO);

    List<SysUserVO> listAllSysUser();

    SysUserVO getUserById(UUID id);

    Boolean updateUser(SysUserDTO sysUserDTO);

    Boolean removeUserById(UUID id);

    Integer removeUserByIds(List<UUID> ids);

    Boolean assignRoles(UUID userId, List<UUID> roleIds);

    List<SysRoleVO> getUserRoles(UUID userId);

    SysUserVO addUser(SysUserAdminDTO sysUserAdminDTO);

    SysUserVO updateProfile(SysUserProfileDTO sysUserProfileDTO);

    SysUserInternalVO selectUserByUsername(String username);

    SysUserInternalVO getUserInfoById(UUID userId);

    List<SysPermissionVO> getUserPermissions(UUID userId);

    Boolean registerUser(SysUserRegisterDTO sysUserRegisterDTO);

    Boolean updatePassword(SysUserPasswordDTO sysUserPasswordDTO);

    Boolean updatePasswordByUserId(UUID userId, String newPassword);

    Boolean resetPassword(UUID userId);

    Boolean isMobileExists(String mobile);

    Boolean isUsernameAvailable(String username);

    SysUserInternalVO mobileLogin(SysUserMobileLoginDTO mobileLoginDTO);

    ThirdPartyLoginResultVO findOrCreateByThirdParty(String provider, String providerId, String username, String email, String name, String avatarUrl);
}
