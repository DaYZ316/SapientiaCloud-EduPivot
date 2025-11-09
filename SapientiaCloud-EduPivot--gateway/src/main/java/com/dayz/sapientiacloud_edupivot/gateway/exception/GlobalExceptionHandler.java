package com.dayz.sapientiacloud_edupivot.gateway.exception;

import com.dayz.sapientiacloud_edupivot.gateway.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.gateway.result.Result;
import com.dayz.sapientiacloud_edupivot.gateway.utils.EnumUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String USER = "user";

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        ResultEnum resultEnum = EnumUtil.getByAttribute(ResultEnum.class, e.getMessage(), ResultEnum::getMessage);
        return Result.fail(Objects.requireNonNullElse(resultEnum, ResultEnum.SYSTEM_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException() {
        return Result.fail(ResultEnum.SYSTEM_ERROR);
    }
} 