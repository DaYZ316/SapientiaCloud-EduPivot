package com.dayz.sapientiacloud_edupivot.auth.exception;

import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        // 根据异常代码设置不同的响应状态
        HttpStatus status = getHttpStatusByCode(ex.getCode());

        log.warn("业务异常 - URI: {}, 错误码: {}, 错误信息: {}", requestUri, ex.getCode(), ex.getMessage());

        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("参数校验失败: {}", errorMessage);
        return Result.fail(400, errorMessage);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("数据绑定失败: {}", errorMessage);
        return Result.fail(400, errorMessage);
    }

    /**
     * 处理JWT相关异常
     */
    @ExceptionHandler(JwtAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Object> handleJwtException(JwtAuthenticationException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.warn("JWT认证失败 - URI: {}, 错误: {}", requestUri, ex.getMessage());
        return Result.fail(401, "认证失败: " + ex.getMessage());
    }

    /**
     * 处理其他运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("运行时异常 - URI: {}, 错误: {}", requestUri, ex.getMessage(), ex);
        return Result.fail(500, "服务器内部错误");
    }

    /**
     * 处理参数非法异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("参数非法异常: {}", ex.getMessage());
        return Result.fail(400, ex.getMessage());
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleGenericException(Exception ex, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        log.error("未知异常 - URI: {}, 错误: {}", requestUri, ex.getMessage(), ex);
        return Result.fail(500, "服务器内部错误");
    }

    /**
     * 根据业务异常代码获取对应的HTTP状态码
     */
    private HttpStatus getHttpStatusByCode(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 422 -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}