package com.dayz.sapientiacloud_edupivot.teacher.common.security.utils;

import com.dayz.sapientiacloud_edupivot.teacher.common.clients.SysPermissionClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 权限表达式工具类
 * 提供用于Spring Security的SpEL权限表达式
 */
@Component("perm")
public class PermissionExpressionUtil {

    private final SysPermissionClient sysPermissionClient;

    public PermissionExpressionUtil(SysPermissionClient sysPermissionClient) {
        this.sysPermissionClient = sysPermissionClient;
    }

    /**
     * 验证当前用户是否已登录
     *
     * @return 是否已登录
     */
    public boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    /**
     * 验证当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public boolean isAdmin() {
        return UserContextUtil.isAdmin();
    }

    /**
     * 验证当前用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    public boolean hasPermission(String permission) {
        return sysPermissionClient.hasPermission(permission).getData();
    }

    /**
     * 验证当前用户是否拥有任一指定权限
     *
     * @param permissions 权限标识列表
     * @return 是否拥有任一权限
     */
    public boolean hasAnyPermission(String... permissions) {
        return sysPermissionClient.hasAnyPermission(Arrays.stream(permissions).toList()).getData();
    }

    /**
     * 验证当前用户是否拥有所有指定权限
     *
     * @param permissions 权限标识列表
     * @return 是否拥有所有权限
     */
    public boolean hasAllPermissions(String... permissions) {
        return sysPermissionClient.hasAllPermissions(Arrays.stream(permissions).toList()).getData();
    }
} 