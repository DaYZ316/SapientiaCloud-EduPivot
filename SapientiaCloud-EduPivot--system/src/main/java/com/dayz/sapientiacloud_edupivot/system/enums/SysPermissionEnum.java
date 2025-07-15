package com.dayz.sapientiacloud_edupivot.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysPermissionEnum {

    PERMISSION_NOT_FOUND(3001, "权限不存在"),
    PERMISSION_ALREADY_EXISTS(3002, "权限已存在"),
    PERMISSION_NAME_CANNOT_BE_EMPTY(3003, "权限名称不能为空"),
    PERMISSION_KEY_CANNOT_BE_EMPTY(3004, "权限标识不能为空"),
    PERMISSION_ID_CANNOT_BE_EMPTY(3005, "权限ID不能为空");

    private final int code;
    private final String message;
} 