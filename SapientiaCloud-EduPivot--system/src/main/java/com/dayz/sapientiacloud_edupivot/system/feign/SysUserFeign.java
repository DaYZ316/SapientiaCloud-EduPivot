package com.dayz.sapientiacloud_edupivot.system.feign;

import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserAdminDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserInternalDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.service.impl.SysUserServiceImpl;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserFeign {

    private final SysUserServiceImpl sysUserService;

    @HasPermission(
            summary = "内部接口 - 获取用户角色列表",
            description = "获取指定用户的角色列表",
            hidden = true
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
            description = "获取指定用户的权限列表",
            hidden = true
    )
    @GetMapping("/internal/{userId}/permission")
    public Result<List<SysPermissionVO>> getUserPermissions(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId
    ) {
        List<SysPermissionVO> permissions = sysUserService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @HasPermission(
            summary = "内部接口 - 管理员添加新用户",
            description = "管理员添加系统用户",
            hidden = true
    )
    @PostMapping("/internal/add")
    public Result<SysUserVO> addSysUser(@RequestBody SysUserAdminDTO sysUserAdminDTO) {
        return Result.success(sysUserService.addUser(sysUserAdminDTO));
    }

    @HasPermission(
            summary = "内部接口 - 根据用户名获取用户信息",
            description = "通过用户名获取用户详细信息",
            hidden = true
    )
    @GetMapping("/internal/info/{username}")
    public Result<SysUserInternalDTO> getUserInfoByUsername(@PathVariable("username") String username) {
        SysUserInternalDTO sysUserInternalDTO = sysUserService.selectUserByUsername(username);
        return Result.success(sysUserInternalDTO);
    }

    @HasPermission(
            summary = "内部接口 - 更新用户信息",
            description = "内部接口，更新用户信息",
            hidden = true
    )
    @PutMapping("/internal/update")
    public Result<Boolean> updateUserInternal(@RequestBody SysUserDTO sysUserDTO) {
        return Result.success(sysUserService.updateUser(sysUserDTO));
    }
}
