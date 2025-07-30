package com.dayz.sapientiacloud_edupivot.system.common.security.service;

import com.dayz.sapientiacloud_edupivot.system.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserPermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务类
 * 提供权限验证和管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserPermissionMapper sysUserPermissionMapper;

    /**
     * 用户权限缓存
     * key: 用户ID, value: 权限标识列表
     */
    private final Map<UUID, List<String>> userPermissionCache = new HashMap<>();

    /**
     * 验证用户是否有指定权限
     *
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        // 如果是管理员，直接返回true
        if (UserContextUtil.isAdmin()) {
            return true;
        }

        // 获取当前用户ID
        UUID userId = UserContextUtil.getCurrentUserId();

        // 从缓存获取用户权限
        List<String> permissions = getUserPermissions(userId);

        // 验证用户是否有指定权限
        return permissions.contains(permission);
    }

    /**
     * 验证用户是否有任一指定权限
     *
     * @param permissions 权限标识列表
     * @return 是否有任一权限
     */
    public boolean hasAnyPermission(String... permissions) {
        // 如果是管理员，直接返回true
        if (UserContextUtil.isAdmin()) {
            return true;
        }

        // 获取当前用户ID
        UUID userId = UserContextUtil.getCurrentUserId();

        // 从缓存获取用户权限
        List<String> userPermissions = getUserPermissions(userId);

        // 验证用户是否有任一指定权限
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证用户是否有所有指定权限
     *
     * @param permissions 权限标识列表
     * @return 是否有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        // 如果是管理员，直接返回true
        if (UserContextUtil.isAdmin()) {
            return true;
        }

        // 获取当前用户ID
        UUID userId = UserContextUtil.getCurrentUserId();

        // 从缓存获取用户权限
        List<String> userPermissions = getUserPermissions(userId);

        // 验证用户是否有所有指定权限
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取用户权限列表
     * 首先尝试从缓存中获取，如果没有则从数据库获取并缓存
     *
     * @param userId 用户ID
     * @return 权限标识列表
     */
    public List<String> getUserPermissions(UUID userId) {
        // 首先尝试从缓存中获取
        List<String> permissions = userPermissionCache.get(userId);
        if (permissions != null) {
            return permissions;
        }

        // 从数据库获取用户权限
        List<SysPermissionVO> permissionVOS = sysUserPermissionMapper.getUserPermissions(userId);
        if (CollectionUtils.isEmpty(permissionVOS)) {
            return Collections.emptyList();
        }

        // 提取权限标识
        permissions = permissionVOS.stream()
                .map(SysPermissionVO::getPermissionKey)
                .collect(Collectors.toList());

        // 缓存用户权限
        userPermissionCache.put(userId, permissions);

        return permissions;
    }

    /**
     * 清除用户权限缓存
     * 当用户权限发生变更时，需要调用此方法清除缓存
     *
     * @param userId 用户ID
     */
    public void clearUserPermissionCache(UUID userId) {
        userPermissionCache.remove(userId);
    }

    /**
     * 清除所有用户权限缓存
     * 当权限体系发生变更时，需要调用此方法清除所有缓存
     */
    public void clearAllPermissionCache() {
        userPermissionCache.clear();
    }
} 