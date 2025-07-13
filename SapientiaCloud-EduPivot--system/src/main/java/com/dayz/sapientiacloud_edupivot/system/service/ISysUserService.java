package com.dayz.sapientiacloud_edupivot.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserAdminDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.github.pagehelper.PageInfo;

import java.util.UUID;

public interface ISysUserService extends IService<SysUser> {
    PageInfo<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO);

    SysUserVO getUserById(UUID id);

    SysUserVO addUser(SysUserAdminDTO sysUserAdminDTO);

    SysUserVO updateUser(SysUserDTO sysUserDTO);

    Boolean deleteUser(UUID id);

    SysUserVO registerUser(SysUserRegisterDTO sysUserRegisterDTO);

    SysUser selectUserByUsername(String username);
}