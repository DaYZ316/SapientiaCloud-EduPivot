package com.dayz.sapientiacloud_edupivot.system.service;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ISysUserService {

    PageInfo<SysUserVO> listSysUserPage(SysUserQueryDTO sysUserQueryDTO);

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

    List<SysPermissionVO> getUserPermissions(UUID userId);

    Boolean registerUser(SysUserRegisterDTO sysUserRegisterDTO);

    Boolean updatePassword(SysUserPasswordDTO sysUserPasswordDTO);

    /**
     * 验证手机号码是否已存在
     *
     * @param mobile 手机号码
     * @return true-已存在，false-不存在
     */
    Boolean isMobileExists(String mobile);

    /**
     * 手机验证码登录
     *
     * @param mobileLoginDTO 手机验证码登录请求
     * @return 用户信息
     */
    SysUserInternalVO mobileLogin(SysUserMobileLoginDTO mobileLoginDTO);
}
