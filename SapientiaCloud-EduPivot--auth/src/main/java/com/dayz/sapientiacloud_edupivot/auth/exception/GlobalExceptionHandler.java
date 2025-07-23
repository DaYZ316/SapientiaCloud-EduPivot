package com.dayz.sapientiacloud_edupivot.auth.exception;

import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.result.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.utils.EnumLookupUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String USER = "user";

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        if (e.getMessage().contains(USER)) {
            EnumLookupUtil.getByAttribute(SysUserEnum.class, e.getMessage(),SysUserEnum::getMessage);
        }else if (e.getMessage().contains(ResultEnum.FORBIDDEN.getMessage())) {
            return Result.fail(ResultEnum.FORBIDDEN);
        }
        return Result.fail(ResultEnum.SYSTEM_ERROR);
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
} 