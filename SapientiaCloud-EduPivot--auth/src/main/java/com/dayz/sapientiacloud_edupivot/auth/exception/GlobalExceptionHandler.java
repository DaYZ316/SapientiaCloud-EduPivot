package com.dayz.sapientiacloud_edupivot.auth.exception;

import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.utils.EnumUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String USER = "user";

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        // 如果BusinessException已经包含code和message，直接使用
        if (e.getCode() != ResultEnum.FAIL.getCode() || !Objects.equals(e.getMessage(), ResultEnum.FAIL.getMessage())) {
            return Result.fail(e.getCode(), e.getMessage());
        }
        
        // 尝试匹配SysUserEnum
        SysUserEnum sysUserEnum = EnumUtil.getByAttribute(SysUserEnum.class, e.getMessage(), SysUserEnum::getMessage);
        if (sysUserEnum != null) {
            return Result.fail(sysUserEnum.getMessage());
        }
        
        // 尝试匹配OAuth2Enum
        OAuth2Enum oAuth2Enum = EnumUtil.getByAttribute(OAuth2Enum.class, e.getMessage(), OAuth2Enum::getMessage);
        if (oAuth2Enum != null) {
            return Result.fail(oAuth2Enum.getCode(), oAuth2Enum.getMessage());
        }
        
        // 尝试匹配ResultEnum
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
} 