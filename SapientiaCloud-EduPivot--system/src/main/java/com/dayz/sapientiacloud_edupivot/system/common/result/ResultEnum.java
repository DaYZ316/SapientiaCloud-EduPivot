package com.dayz.sapientiacloud_edupivot.system.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultEnum {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "登录超时,请重新登录"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    SYSTEM_ERROR(1003, "系统异常");

    private final int code;

    private final String message;
} 