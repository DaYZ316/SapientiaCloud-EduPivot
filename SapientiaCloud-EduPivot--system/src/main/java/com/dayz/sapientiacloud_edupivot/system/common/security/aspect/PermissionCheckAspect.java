package com.dayz.sapientiacloud_edupivot.system.common.security.aspect;

import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.common.result.ResultEnum;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserPermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * 权限检查切面
 * 拦截标记了@HasPermission注解的方法，检查当前用户是否拥有所需权限
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionCheckAspect {

    private final SysUserPermissionMapper sysUserPermissionMapper;

    /**
     * 在执行带有@HasPermission注解的方法前检查权限
     */
    @Before("@annotation(com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 如果是管理员，直接放行
        if (UserContextUtil.isAdmin()) {
            return;
        }

        // 获取方法上的HasPermission注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);

        // 如果没有指定权限，则不进行权限校验
        if (hasPermission == null || !StringUtils.hasText(hasPermission.permission())) {
            return;
        }

        // 获取当前用户ID
        UUID userId = UserContextUtil.getCurrentUserId();

        // 获取用户所有权限
        List<SysPermissionVO> permissions = sysUserPermissionMapper.getUserPermissions(userId);
        List<String> permissionKeys = permissions.stream()
                .map(SysPermissionVO::getPermissionKey)
                .toList();

        // 检查是否有所需权限
        if (!permissionKeys.contains(hasPermission.permission())) {
            log.warn("用户 {} 没有所需权限 {}", UserContextUtil.getCurrentUsername(), hasPermission.permission());
            throw new BusinessException(ResultEnum.FORBIDDEN);
        }
    }
} 