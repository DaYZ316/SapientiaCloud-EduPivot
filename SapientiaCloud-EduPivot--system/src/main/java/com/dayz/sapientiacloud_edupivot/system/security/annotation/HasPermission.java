package com.dayz.sapientiacloud_edupivot.system.security.annotation;

import io.swagger.v3.oas.annotations.Operation;

import java.lang.annotation.*;

/**
 * 权限控制注解，继承自Operation
 * 同时提供Swagger文档和权限控制功能
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
public @interface HasPermission {
    
    /**
     * 权限字符串，用于权限校验
     * @return 权限字符串
     */
    String permission() default "";
    
    /**
     * 接口摘要
     * @return 接口摘要
     */
    String summary() default "";
    
    /**
     * 接口描述
     * @return 接口详细描述
     */
    String description() default "";
    
    /**
     * 是否隐藏文档
     * @return true表示隐藏
     */
    boolean hidden() default false;
} 