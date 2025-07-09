package com.dayz.sapientiacloud_edupivot.auth.exception;

import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常: {}", e.getMessage());
        return Result.fail(ResultEnum.UNAUTHORIZED.getCode(), "认证失败: " + e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.error("凭据异常: {}", e.getMessage());
        return Result.fail(ResultEnum.UNAUTHORIZED.getCode(), "用户名或密码错误");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("访问拒绝: {}", e.getMessage());
        return Result.fail(ResultEnum.FORBIDDEN.getCode(), "无权访问");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.fail(ResultEnum.FAIL.getCode(), "系统错误: " + e.getMessage());
    }
} 