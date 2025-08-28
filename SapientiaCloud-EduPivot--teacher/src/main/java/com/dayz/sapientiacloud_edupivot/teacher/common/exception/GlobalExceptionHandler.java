package com.dayz.sapientiacloud_edupivot.teacher.common.exception;

import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.utils.EnumUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String USER = "user";

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        SysUserEnum sysUserEnum = EnumUtil.getByAttribute(SysUserEnum.class, e.getMessage(), SysUserEnum::getMessage);
        if (sysUserEnum != null) {
            return Result.fail(sysUserEnum.getMessage());
        }
        SysRoleEnum sysRoleEnum = EnumUtil.getByAttribute(SysRoleEnum.class, e.getMessage(), SysRoleEnum::getMessage);
        if (sysRoleEnum != null) {
            return Result.fail(sysRoleEnum.getMessage());
        }
        SysPermissionEnum sysPermissionEnum = EnumUtil.getByAttribute(SysPermissionEnum.class, e.getMessage(), SysPermissionEnum::getMessage);
        if (sysPermissionEnum != null) {
            return Result.fail(sysPermissionEnum.getMessage());
        }
        ResultEnum resultEnum = EnumUtil.getByAttribute(ResultEnum.class, e.getMessage(), ResultEnum::getMessage);
        return Result.fail(Objects.requireNonNullElse(resultEnum, ResultEnum.SYSTEM_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage());
        if (e.getMessage().contains(USER)) {
            if (e.getMessage().contains(SysUserEnum.USER_NOT_FOUND.getMessage())) {
                return Result.fail(SysUserEnum.USER_NOT_FOUND.getMessage());
            } else {
                return Result.fail(SysUserEnum.USER_SERVICE_ERROR.getMessage());
            }
        }
        return Result.fail(ResultEnum.SYSTEM_ERROR.getCode(), ResultEnum.SYSTEM_ERROR.getMessage() + ": " + e.getMessage());
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限异常: {}", e.getMessage());
        return Result.fail(ResultEnum.FORBIDDEN);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "验证失败" : fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing + "; " + replacement
                ));
        log.warn("参数验证异常: {}", errors);
        return Result.fail("参数验证失败");
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "验证失败" : fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing + "; " + replacement
                ));
        log.warn("参数绑定异常: {}", errors);
        return Result.fail("参数绑定失败");
    }
} 