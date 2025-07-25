package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysRoleService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "角色管理", description = "用于管理系统角色的API")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController extends BaseController {

    private final ISysRoleService sysRoleService;

    @HasPermission(
            summary = "分页查询角色",
            description = "根据传入的条件分页查询角色信息。支持根据角色名称、角色标识等字段进行模糊查询。",
            permission = PermissionConstants.ROLE_QUERY
    )
    @GetMapping("/list")
    public TableDataResult sysRoleList(@ParameterObject SysRoleQueryDTO sysRoleQueryDTO) {
        startPage();
        PageInfo<SysRoleVO> pageInfo = sysRoleService.listSysRole(sysRoleQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "根据ID获取角色",
            description = "通过角色的唯一ID获取其详细信息。",
            permission = PermissionConstants.ROLE_QUERY
    )
    @GetMapping("/{id}")
    public Result<SysRoleVO> getRoleById(
            @Parameter(name = "id", description = "角色ID", required = true) @PathVariable("id") UUID id
    ) {
        SysRoleVO sysRoleVO = sysRoleService.getRoleById(id);
        return Result.success(sysRoleVO);
    }

    @HasPermission(
            summary = "添加新角色",
            description = "添加一个新的角色到系统中。",
            permission = PermissionConstants.ROLE_ADD
    )
    @PostMapping
    public Result<Boolean> addRole(@RequestBody SysRoleAddDTO sysRoleDTO) {
        return Result.success(sysRoleService.addRole(sysRoleDTO));
    }

    @HasPermission(
            summary = "更新现有角色",
            description = "修改现有角色的信息。",
            permission = PermissionConstants.ROLE_EDIT
    )
    @PutMapping
    public Result<Boolean> updateRole(@RequestBody SysRoleDTO sysRoleDTO) {
        return Result.success(sysRoleService.updateRole(sysRoleDTO));
    }

    @HasPermission(
            summary = "删除角色",
            description = "根据角色ID从系统中移除角色。",
            permission = PermissionConstants.ROLE_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeRole(
            @Parameter(name = "id", description = "角色ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysRoleService.removeRoleById(id));
    }

    @HasPermission(
            summary = "批量删除角色",
            description = "根据角色ID列表批量删除角色。",
            permission = PermissionConstants.ROLE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeRoles(
            @Parameter(name = "ids", description = "角色ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(sysRoleService.removeRoleByIds(ids));
    }

    @HasPermission(
            summary = "分配角色权限",
            description = "为指定角色分配权限。",
            permission = PermissionConstants.ROLE_EDIT
    )
    @PostMapping("/{roleId}/permission")
    public Result<Boolean> assignRolePermissions(
            @Parameter(name = "roleId", description = "角色ID", required = true) @PathVariable("roleId") UUID roleId,
            @Parameter(name = "permissionIds", description = "权限ID列表", required = true) @RequestBody List<UUID> permissionIds
    ) {
        return Result.success(sysRoleService.assignRolePermissions(roleId, permissionIds));
    }
}
