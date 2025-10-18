package com.dayz.sapientiacloud_edupivot.teacher.common.exception;

import com.dayz.sapientiacloud_edupivot.teacher.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import com.dayz.sapientiacloud_edupivot.teacher.common.utils.EnumUtil;
import com.dayz.sapientiacloud_edupivot.teacher.enums.TeacherEnum;
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
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
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
        TeacherEnum teacherEnum = EnumUtil.getByAttribute(TeacherEnum.class, e.getMessage(), TeacherEnum::getMessage);
        if (teacherEnum != null) {
            return Result.fail(teacherEnum.getMessage());
        }
        ResultEnum resultEnum = EnumUtil.getByAttribute(ResultEnum.class, e.getMessage(), ResultEnum::getMessage);
        return Result.fail(Objects.requireNonNullElse(resultEnum, ResultEnum.SYSTEM_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException() {
        return Result.fail(ResultEnum.SYSTEM_ERROR);
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException() {
        return Result.fail(ResultEnum.FORBIDDEN);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        return Result.fail("参数验证失败: " + e.getMessage());
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        return Result.fail("参数绑定失败:" + e.getMessage());
    }
} 