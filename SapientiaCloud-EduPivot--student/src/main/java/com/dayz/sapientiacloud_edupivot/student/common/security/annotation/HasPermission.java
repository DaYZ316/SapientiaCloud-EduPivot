package com.dayz.sapientiacloud_edupivot.student.common.security.annotation;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation
public @interface HasPermission {

    String permission() default "";

    @AliasFor(annotation = Operation.class, attribute = "summary")
    String summary() default "";

    @AliasFor(annotation = Operation.class, attribute = "description")
    String description() default "";

    @AliasFor(annotation = Operation.class, attribute = "hidden")
    boolean hidden() default false;
} 