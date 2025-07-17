package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.service.ISysRoleService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "角色管理", description = "用于管理系统角色的API")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    @HasPermission(
        permission = PermissionConstants.ROLE_QUERY,
        summary = "分页查找角色", 
        description = "根据传入的条件分页查询角色信息。支持根据角色名、角色描述等字段进行模糊查询。"
    )
    @GetMapping("/list")
    public Result<PageInfo<SysRoleVO>> listSysRole(@ParameterObject SysRoleQueryDTO sysRoleQueryDTO) {
        PageInfo<SysRoleVO> list = sysRoleService.listSysRole(sysRoleQueryDTO);
        return Result.success(list);
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_QUERY,
        summary = "根据ID获取角色", 
        description = "通过角色ID获取其详细信息和权限列表。"
    )
    @GetMapping("/{id}")
    public Result<SysRoleVO> getRoleById(
            @Parameter(name = "id", description = "角色ID", required = true) @PathVariable("id") UUID id
    ) {
        SysRoleVO sysRoleVO = sysRoleService.getRoleById(id);
        return Result.success(sysRoleVO);
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_ADD,
        summary = "添加新角色", 
        description = "管理员向系统中添加一个新角色。"
    )
    @PostMapping("/add")
    public Result<Boolean> addRole(@Valid @RequestBody SysRoleAddDTO sysRoleDTO) {
        return Result.success(sysRoleService.addRole(sysRoleDTO));
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_EDIT,
        summary = "更新现有角色", 
        description = "修改现有角色的信息。"
    )
    @PutMapping
    public Result<Boolean> updateRole(@Valid @RequestBody SysRoleDTO sysRoleDTO) {
        return Result.success(sysRoleService.updateRole(sysRoleDTO));
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_DELETE,
        summary = "删除角色", 
        description = "根据角色ID从系统中移除角色。"
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeRole(
            @Parameter(name = "id", description = "角色ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysRoleService.removeRoleById(id));
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_DELETE,
        summary = "批量删除角色", 
        description = "根据角色ID列表批量删除角色。"
    )
    @DeleteMapping
    public Result<Integer> removeRoles(
            @Parameter(name = "ids", description = "角色ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(sysRoleService.removeRoleByIds(ids));
    }

    @HasPermission(
        permission = PermissionConstants.ROLE_EDIT,
        summary = "分配角色权限", 
        description = "为指定角色分配权限。"
    )
    @PostMapping("/{id}/permission")
    public Result<Boolean> assignRolePermissions(
            @Parameter(name = "id", description = "角色ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "permissionIds", description = "权限ID列表", required = true) @RequestBody List<UUID> permissionIds
    ) {
        return Result.success(sysRoleService.assignRolePermissions(id, permissionIds));
    }
}
