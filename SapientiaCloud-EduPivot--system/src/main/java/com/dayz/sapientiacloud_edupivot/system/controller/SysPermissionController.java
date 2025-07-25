package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysPermissionService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "权限管理", description = "用于管理系统权限的API")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class SysPermissionController extends BaseController {

    private final ISysPermissionService sysPermissionService;

    @HasPermission(
        permission = PermissionConstants.PERMISSION_QUERY, 
        summary = "分页查找权限", 
        description = "根据传入的条件分页查询权限信息。支持根据权限名称、权限标识等字段进行模糊查询。"
    )
    @GetMapping("/list")
    public TableDataResult sysPermissionList(@ParameterObject SysPermissionQueryDTO sysPermissionQueryDTO) {
        startPage();
        PageInfo<SysPermissionVO> pageInfo = sysPermissionService.listSysPermission(sysPermissionQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
        permission = PermissionConstants.PERMISSION_QUERY,
        summary = "根据ID获取权限", 
        description = "通过权限的唯一ID获取其详细信息。"
    )
    @GetMapping("/{id}")
    public Result<SysPermission> getPermissionById(
            @Parameter(name = "id", description = "权限ID", required = true) @PathVariable("id") UUID id
    ) {
        SysPermission sysPermission = sysPermissionService.getPermissionById(id);
        return Result.success(sysPermission);
    }

    @HasPermission(
        permission = PermissionConstants.PERMISSION_ADD,
        summary = "添加权限", 
        description = "添加新的权限信息。"
    )
    @PostMapping
    public Result<Boolean> addPermission(@Valid @RequestBody SysPermissionAddDTO sysPermissionDTO) {
        return Result.success(sysPermissionService.addPermission(sysPermissionDTO));
    }

    @HasPermission(
        permission = PermissionConstants.PERMISSION_EDIT,
        summary = "更新现有权限", 
        description = "修改现有权限的信息。"
    )
    @PutMapping
    public Result<Boolean> updatePermission(@Valid @RequestBody SysPermissionDTO sysPermissionDTO) {
        return Result.success(sysPermissionService.updatePermission(sysPermissionDTO));
    }

    @HasPermission(
        permission = PermissionConstants.PERMISSION_DELETE,
        summary = "删除权限", 
        description = "根据权限ID从系统中移除权限。"
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removePermission(
            @Parameter(name = "id", description = "权限ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysPermissionService.removePermissionById(id));
    }

    @HasPermission(
        permission = PermissionConstants.PERMISSION_DELETE,
        summary = "批量删除权限", 
        description = "根据权限ID列表批量删除权限。"
    )
    @DeleteMapping
    public Result<Integer> removePermissions(
            @Parameter(name = "ids", description = "权限ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(sysPermissionService.removePermissionByIds(ids));
    }
} 