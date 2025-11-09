package com.dayz.sapientiacloud_edupivot.minio.exception;

import com.dayz.sapientiacloud_edupivot.minio.enums.FileEnum;
import com.dayz.sapientiacloud_edupivot.minio.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.minio.result.Result;
import com.dayz.sapientiacloud_edupivot.minio.utils.EnumUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;
import java.util.Objects;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        FileEnum fileEnum = EnumUtil.getByAttribute(FileEnum.class, e.getMessage(), FileEnum::getMessage);
        if (fileEnum != null) {
            return Result.fail(fileEnum.getMessage());
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


    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException() {
        return Result.fail(FileEnum.FILE_SIZE_LIMIT_EXCEEDED.getMessage());
    }

    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMultipartException() {
        return Result.fail(FileEnum.FILE_UPLOAD_FAILED.getMessage());
    }
} 