package com.dayz.sapientiacloud_edupivot.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysRoleEnum {

    ROLE_NOT_FOUND(2001, "角色不存在"),
    ROLE_ALREADY_EXISTS(2002, "角色已存在"),
    ROLE_NAME_CANNOT_BE_EMPTY(2003, "角色名称不能为空"),
    ROLE_KEY_CANNOT_BE_EMPTY(2004, "角色标识不能为空"),
    ADMIN_OPERATION_FORBIDDEN(2005, "管理员角色禁止操作");

    private final int code;
    private final String message;
} 