package com.dayz.sapientiacloud_edupivot.system.feigns;

import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.service.PermissionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "权限验证内部接口", description = "权限验证内部管理接口")
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Slf4j
public class SysPermissionFeign {

    private final PermissionService permissionService;

    @HasPermission(
            summary = "内部接口 - 验证用户权限",
            description = "验证当前用户是否有指定权限"
    )
    @GetMapping("/internal/check/{permission}")
    public Result<Boolean> hasPermission(
            @Parameter(name = "permission", description = "权限标识", required = true)
            @PathVariable("permission") String permission
    ) {
        boolean hasPermission = permissionService.hasPermission(permission);
        return Result.success(hasPermission);
    }

    @HasPermission(
            summary = "内部接口 - 验证用户任一权限",
            description = "验证当前用户是否有任一指定权限"
    )
    @PostMapping("/internal/check/any")
    public Result<Boolean> hasAnyPermission(
            @Parameter(name = "permissions", description = "权限标识列表", required = true)
            @RequestBody List<String> permissions
    ) {
        boolean hasAnyPermission = permissionService.hasAnyPermission(permissions.toArray(new String[0]));
        return Result.success(hasAnyPermission);
    }

    @HasPermission(
            summary = "内部接口 - 验证用户所有权限",
            description = "验证当前用户是否有所有指定权限"
    )
    @PostMapping("/internal/check/all")
    public Result<Boolean> hasAllPermissions(
            @Parameter(name = "permissions", description = "权限标识列表", required = true)
            @RequestBody List<String> permissions
    ) {
        boolean hasAllPermissions = permissionService.hasAllPermissions(permissions.toArray(new String[0]));
        return Result.success(hasAllPermissions);
    }

    @HasPermission(
            summary = "内部接口 - 获取用户权限列表",
            description = "获取指定用户的所有权限标识列表"
    )
    @GetMapping("/internal/{userId}/permissions")
    public Result<List<String>> getUserPermissions(
            @Parameter(name = "userId", description = "用户ID", required = true)
            @PathVariable("userId") UUID userId
    ) {
        List<String> permissions = permissionService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @HasPermission(
            summary = "内部接口 - 清除用户权限缓存",
            description = "清除指定用户的权限缓存"
    )
    @DeleteMapping("/internal/{userId}/cache")
    public Result<Boolean> clearUserPermissionCache(
            @Parameter(name = "userId", description = "用户ID", required = true)
            @PathVariable("userId") UUID userId
    ) {
        try {
            permissionService.clearUserPermissionCache(userId);
            return Result.success(true);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败, userId: {}", userId, e);
            return Result.success(false);
        }
    }

    @HasPermission(
            summary = "内部接口 - 清除所有权限缓存",
            description = "清除所有用户的权限缓存"
    )
    @DeleteMapping("/internal/cache/all")
    public Result<Boolean> clearAllPermissionCache() {
        try {
            permissionService.clearAllPermissionCache();
            return Result.success(true);
        } catch (Exception e) {
            log.error("清除所有权限缓存失败", e);
            return Result.success(false);
        }
    }
}