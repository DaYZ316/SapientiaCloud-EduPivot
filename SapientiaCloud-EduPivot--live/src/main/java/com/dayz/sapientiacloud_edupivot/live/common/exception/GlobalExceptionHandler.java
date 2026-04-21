package com.dayz.sapientiacloud_edupivot.live.common.exception;

import com.dayz.sapientiacloud_edupivot.live.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.live.common.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.live.common.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.live.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import com.dayz.sapientiacloud_edupivot.live.common.utils.EnumUtil;
import com.dayz.sapientiacloud_edupivot.live.enums.LiveRoomEnum;
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

@RestControllerAdvice
@Slf4j
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
        LiveRoomEnum liveRoomEnum = EnumUtil.getByAttribute(LiveRoomEnum.class, e.getMessage(), LiveRoomEnum::getMessage);
        if (liveRoomEnum != null) {
            return Result.fail(liveRoomEnum.getMessage());
        }
        ResultEnum resultEnum = EnumUtil.getByAttribute(ResultEnum.class, e.getMessage(), ResultEnum::getMessage);
        return Result.fail(Objects.requireNonNullElse(resultEnum, ResultEnum.SYSTEM_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("System exception: ", e);
        return Result.fail(ResultEnum.SYSTEM_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException() {
        return Result.fail(ResultEnum.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        return Result.fail("参数校验失败: " + e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        return Result.fail("参数绑定失败: " + e.getMessage());
    }
}
