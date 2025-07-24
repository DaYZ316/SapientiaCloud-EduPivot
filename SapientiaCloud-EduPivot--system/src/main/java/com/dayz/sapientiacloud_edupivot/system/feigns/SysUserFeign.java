package com.dayz.sapientiacloud_edupivot.system.feigns;

import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "用户内部接口", description = "用户内部管理接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class SysUserFeign {

    private final ISysUserService sysUserService;

    @HasPermission(
            summary = "内部接口 - 获取用户角色列表",
            description = "获取指定用户的角色列表"
    )
    @GetMapping("/internal/{userId}/role")
    public Result<List<SysRoleVO>> getUserRoles(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId
    ) {
        List<SysRoleVO> roles = sysUserService.getUserRoles(userId);
        return Result.success(roles);
    }

    @HasPermission(
            summary = "内部接口 - 获取用户权限列表",
            description = "获取指定用户的权限列表"
    )
    @GetMapping("/internal/{userId}/permission")
    public Result<List<SysPermissionVO>> getUserPermissions(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId
    ) {
        List<SysPermissionVO> permissions = sysUserService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @HasPermission(
            summary = "内部接口 - 根据用户名获取用户信息",
            description = "通过用户名获取用户详细信息"
    )
    @GetMapping("/internal/info/{username}")
    public Result<SysUserInternalVO> getUserInfoByUsername(@PathVariable("username") String username) {
        SysUserInternalVO sysUserInternalVO = sysUserService.selectUserByUsername(username);
        return Result.success(sysUserInternalVO);
    }


    @HasPermission(
            summary = "内部接口 - 更新用户信息",
            description = "内部接口，更新用户信息"
    )
    @PutMapping("/internal/update")
    public Result<Boolean> updateUserInternal(@RequestBody SysUserDTO sysUserDTO) {
        return Result.success(sysUserService.updateUser(sysUserDTO));
    }

    @HasPermission(
            summary = "内部接口 - 注册用户",
            description = "用户UI端注册用户。"
    )
    @PostMapping("/internal/register")
    public Result<Boolean> registerUser(@RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return Result.success(sysUserService.registerUser(sysUserRegisterDTO));
    }

    @HasPermission(
            summary = "内部接口 - 更新密码",
            description = "更新用户密码"
    )
    @PutMapping("/internal/password")
    public Result<Boolean> updatePassword(@RequestBody SysUserPasswordDTO sysUserPasswordDTO) {
        return Result.success(sysUserService.updatePassword(sysUserPasswordDTO));
    }
}
