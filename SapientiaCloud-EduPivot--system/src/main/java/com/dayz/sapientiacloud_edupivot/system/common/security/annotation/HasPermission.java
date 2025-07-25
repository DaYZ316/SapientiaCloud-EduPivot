package com.dayz.sapientiacloud_edupivot.system.common.security.annotation;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标记需要进行权限校验的方法
 * 此注解同时继承了Swagger的Operation注解，可以同时用于API文档生成
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
public @interface HasPermission {

    /**
     * 权限标识，例如：system:user:add
     * 用户必须拥有此权限才能访问被注解的方法
     */
    String permission() default "";

    /**
     * API接口摘要
     * 用于Swagger文档
     */
    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "";

    /**
     * API接口详细描述
     * 用于Swagger文档
     */
    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    /**
     * 是否在API文档中隐藏此接口
     * 用于Swagger文档
     */
    @AliasFor(annotation = Operation.class, attribute = "hidden")
    boolean hidden() default false;
} 