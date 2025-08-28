package com.dayz.sapientiacloud_edupivot.course.common.security.utils;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 */
@Slf4j
@Component
public class UserContextUtil {

    private static final String ADMIN_ROLE_KEY = "ADMIN";

    private static SysUserClient sysUserClient;

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID (UUID类型)
     */
    public static UUID getCurrentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    try {
                        if (principal instanceof Map) {
                            Object userId = ((Map<?, ?>) principal).get("userId");
                            if (userId != null) {
                                return UUID.fromString(userId.toString());
                            }
                        }
                        return null;
                    } catch (IllegalArgumentException e) {
                        log.error("转换用户ID为UUID失败: {}", e.getMessage());
                        throw new BusinessException("用户ID格式不正确");
                    }
                })
                .orElseThrow(() -> new BusinessException(ResultEnum.UNAUTHORIZED.getMessage()));
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户名
     */
    public static String getCurrentUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails) {
                        return ((UserDetails) principal).getUsername();
                    }
                    if (principal instanceof Map) {
                        return (String) ((Map<?, ?>) principal).get("username");
                    }
                    if (principal instanceof String) {
                        return (String) principal;
                    }
                    return null;
                })
                .orElseThrow(() -> new BusinessException(ResultEnum.UNAUTHORIZED.getMessage()));
    }

    /**
     * 获取当前登录用户的角色列表
     *
     * @return 当前登录用户的角色列表
     */
    public static List<String> getCurrentUserRoles() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new BusinessException(ResultEnum.UNAUTHORIZED.getMessage()));
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户信息
     */
    public static SysUserInternalVO getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }

        Result<SysUserInternalVO> result = sysUserClient.getUserInfoByUsername(username);
        if (result == null || !result.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR.getMessage());
        }

        SysUserInternalVO sysUserInternalVO = result.getData();
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
        }

        return sysUserInternalVO;
    }

    /**
     * 判断当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param roleKey 角色标识
     * @return 是否拥有该角色
     */
    public static boolean hasRole(String roleKey) {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(roleKey));
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole(ADMIN_ROLE_KEY);
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Autowired
    public void setSysUserClient(SysUserClient sysUserClient) {
        UserContextUtil.sysUserClient = sysUserClient;
    }
}