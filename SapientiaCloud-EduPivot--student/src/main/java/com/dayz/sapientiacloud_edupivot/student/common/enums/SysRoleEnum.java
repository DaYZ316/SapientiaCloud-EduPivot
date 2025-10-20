package com.dayz.sapientiacloud_edupivot.student.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysRoleEnum implements BaseEnum {

    ROLE_NOT_FOUND(2001, "角色不存在"),
    ROLE_ALREADY_EXISTS(2002, "角色已存在"),
    ROLE_ID_CANNOT_BE_EMPTY(2002, "角色ID不能为空"),
    ROLE_NAME_CANNOT_BE_EMPTY(2004, "角色名称不能为空"),
    ROLE_KEY_CANNOT_BE_EMPTY(2005, "角色标识不能为空"),
    ADMIN_OPERATION_FORBIDDEN(2006, "超级管理员角色禁止操作"),
    ASSIGN_PERMISSION_FAILED(2007, "分配角色权限失败"),
    ROLE_PERMISSION_DELETE_FAILED(2008, "删除角色权限失败");

    private final int code;
    private final String message;
} 