package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionAddDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysPermissionQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysPermissionService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            summary = "分页查询权限",
            description = "根据传入的条件分页查询权限信息。支持根据权限名称、标识等字段进行模糊查询。",
            permission = PermissionConstants.PERMISSION_QUERY
    )
    @GetMapping("/list")
    public TableDataResult sysPermissionList(@ParameterObject SysPermissionQueryDTO sysPermissionQueryDTO) {
        startPage();
        PageInfo<SysPermissionVO> pageInfo = sysPermissionService.listSysPermission(sysPermissionQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "根据ID获取权限",
            description = "通过权限的唯一ID获取其详细信息。",
            permission = PermissionConstants.PERMISSION_QUERY
    )
    @GetMapping("/{id}")
    public Result<SysPermissionVO> getPermissionById(
            @Parameter(name = "id", description = "权限ID", required = true) @PathVariable("id") UUID id
    ) {
        SysPermissionVO sysPermissionVO = new SysPermissionVO();
        sysPermissionService.getPermissionById(id);
        return Result.success(sysPermissionVO);
    }

    @HasPermission(
            summary = "添加新权限",
            description = "添加一个新的权限到系统中。",
            permission = PermissionConstants.PERMISSION_ADD
    )
    @PostMapping
    public Result<Boolean> addPermission(@RequestBody SysPermissionAddDTO sysPermissionDTO) {
        return Result.success(sysPermissionService.addPermission(sysPermissionDTO));
    }

    @HasPermission(
            summary = "更新现有权限",
            description = "修改现有权限的信息。",
            permission = PermissionConstants.PERMISSION_EDIT
    )
    @PutMapping
    public Result<Boolean> updatePermission(@RequestBody SysPermissionDTO sysPermissionDTO) {
        return Result.success(sysPermissionService.updatePermission(sysPermissionDTO));
    }

    @HasPermission(
            summary = "删除权限",
            description = "根据权限ID从系统中移除权限。",
            permission = PermissionConstants.PERMISSION_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removePermission(
            @Parameter(name = "id", description = "权限ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysPermissionService.removePermissionById(id));
    }

    @HasPermission(
            summary = "批量删除权限",
            description = "根据权限ID列表批量删除权限。",
            permission = PermissionConstants.PERMISSION_DELETE
    )
    @DeleteMapping
    public Result<Integer> removePermissions(
            @Parameter(name = "ids", description = "权限ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(sysPermissionService.removePermissionByIds(ids));
    }
} 