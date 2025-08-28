package com.dayz.sapientiacloud_edupivot.course.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysPermissionEnum implements BaseEnum {

    PERMISSION_NOT_FOUND(3001, "权限不存在"),
    PERMISSION_ALREADY_EXISTS(3002, "权限已存在"),
    PERMISSION_NAME_CANNOT_BE_EMPTY(3003, "权限名称不能为空"),
    PERMISSION_KEY_CANNOT_BE_EMPTY(3004, "权限标识不能为空"),
    PERMISSION_ID_CANNOT_BE_EMPTY(3005, "权限ID不能为空"),
    PERMISSION_KEY_EXISTS(3006, "权限标识已存在"),
    PERMISSION_ACCESS_DENIED(3007, "没有访问该资源的权限");

    private final int code;
    private final String message;
} 