package com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.utils.EnumUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.KnowledgeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String USER = "user";

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
        AIChatEnum aiChatEnum = EnumUtil.getByAttribute(AIChatEnum.class, e.getMessage(), AIChatEnum::getMessage);
        if (aiChatEnum != null) {
            return Result.fail(aiChatEnum.getMessage());
        }
        KnowledgeEnum knowledgeEnum = EnumUtil.getByAttribute(KnowledgeEnum.class, e.getMessage(), KnowledgeEnum::getMessage);
        if (knowledgeEnum != null) {
            return Result.fail(knowledgeEnum.getMessage());
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