package com.dayz.sapientiacloud_edupivot.system.security.aspect;

import com.dayz.sapientiacloud_edupivot.system.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.system.common.result.ResultEnum;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.security.annotation.HasPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 权限验证切面
 * 用于拦截和验证标有@HasPermission注解的方法
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HasPermissionAspect {


    @Before("@annotation(com.dayz.sapientiacloud_edupivot.system.security.annotation.HasPermission)")
    public void checkPermission(JoinPoint joinPoint) {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException(ResultEnum.UNAUTHORIZED);
        }

        // 获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        HasPermission hasPermission = method.getAnnotation(HasPermission.class);
        
        // 获取权限字符串
        String requiredPermission = hasPermission.permission();
        
        // 如果未设置权限，则不进行权限检查
        if (!StringUtils.hasText(requiredPermission)) {
            return;
        }
        
        // 获取用户权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // 检查是否有ADMIN角色
        if (authorities.stream().anyMatch(authority -> SysRoleVO.ADMIN_ROLE_KEY.equals(authority.getAuthority()))) {
            log.debug("用户拥有ADMIN角色，自动通过权限检查");
            return;
        }
        
        // 检查是否有指定权限
        boolean hasRequiredPermission = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(requiredPermission));
        
        if (!hasRequiredPermission) {
            log.warn("用户没有所需权限: {}", requiredPermission);
            throw new BusinessException(ResultEnum.FORBIDDEN);
        }
        
        log.debug("用户通过权限检查: {}", requiredPermission);
    }
} 