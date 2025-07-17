package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserAdminDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "用户管理", description = "用于管理系统用户的API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;

    @HasPermission(
        summary = "分页查找用户", 
        description = "根据传入的条件分页查询用户信息。支持根据用户名、昵称等字段进行模糊查询。", 
        permission = PermissionConstants.USER_QUERY
    )
    @GetMapping("/list")
    public Result<PageInfo<SysUserVO>> listSysUser(@ParameterObject SysUserQueryDTO sysUserQueryDTO) {
        PageInfo<SysUserVO> list = sysUserService.listSysUser(sysUserQueryDTO);
        return Result.success(list);
    }

    @HasPermission(
        summary = "根据ID获取用户", 
        description = "通过用户的唯一ID获取其详细信息。", 
        permission = PermissionConstants.USER_QUERY
    )
    @GetMapping("/{id}")
    public Result<SysUserVO> getUserById(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        SysUserVO sysUserVO = sysUserService.getUserById(id);
        return Result.success(sysUserVO);
    }

    @HasPermission(
        summary = "注册用户", 
        description = "用户UI端注册用户。"
    )
    @PostMapping("/register")
    public Result<Boolean> registerUser(@Valid @RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return Result.success(sysUserService.registerUser(sysUserRegisterDTO));
    }

    @HasPermission(
        summary = "更新现有用户", 
        description = "修改现有用户的信息。", 
        permission = PermissionConstants.USER_EDIT
    )
    @PutMapping
    public Result<Boolean> updateUser(@Valid @RequestBody SysUserDTO sysUserDTO) {
        return Result.success(sysUserService.updateUser(sysUserDTO));
    }

    @HasPermission(
        summary = "删除用户", 
        description = "根据用户ID从系统中移除用户。", 
        permission = PermissionConstants.USER_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeUser(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysUserService.removeUserById(id));
    }

    @HasPermission(
        summary = "批量删除用户", 
        description = "根据用户ID列表批量删除用户。", 
        permission = PermissionConstants.USER_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeUsers(
            @Parameter(name = "ids", description = "用户ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(sysUserService.removeUserByIds(ids));
    }

    @HasPermission(
        summary = "分配用户角色", 
        description = "为指定用户分配角色。", 
        permission = PermissionConstants.USER_EDIT
    )
    @PostMapping("/{userId}/role")
    public Result<Boolean> assignRoles(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId,
            @Parameter(name = "roleIds", description = "角色ID列表", required = true) @RequestBody List<UUID> roleIds
    ) {
        return Result.success(sysUserService.assignRoles(userId, roleIds));
    }

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
    public Result<SysUser> getUserInfoByUsername(@PathVariable("username") String username) {
        SysUser sysUser = sysUserService.selectUserByUsername(username);
        return Result.success(sysUser);
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