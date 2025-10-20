package com.dayz.sapientiacloud_edupivot.system.enums;

import com.dayz.sapientiacloud_edupivot.system.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysUserEnum implements BaseEnum {

    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_ACCOUNT_DISABLED(1003, "用户账号已被禁用"),
    USER_ACCOUNT_LOCKED(1004, "用户账号已被锁定"),
    USER_ACCOUNT_EXPIRED(1005, "用户账号已过期"),
    PASSWORD_INCORRECT(1006, "原密码不正确"),
    USERNAME_ALREADY_EXISTS(1101, "用户已存在"),
    EMAIL_ALREADY_EXISTS(1102, "邮箱地址已被注册"),
    PHONE_NUMBER_ALREADY_EXISTS(1103, "手机号码已被注册"),
    USERNAME_CANNOT_BE_EMPTY(1104, "用户名不能为空"),
    PASSWORD_CANNOT_BE_EMPTY(1105, "密码不能为空"),
    MOBILE_CANNOT_BE_EMPTY(1106, "手机号码不能为空"),
    INSUFFICIENT_PERMISSIONS(1201, "用户权限不足"),
    USER_LOGIN_FAILED(1202, "用户登录失败"),
    USER_LOGOUT_FAILED(1203, "用户登出失败"),
    ASSIGN_ROLE_FAILED(1204, "分配用户角色失败"),
    USER_ROLE_DELETE_FAILED(1205, "删除用户角色失败"),
    USER_SERVICE_ERROR(1206, "用户服务异常"),
    NEW_AND_CONFIRM_PASSWORD_NOT_MATCH(1207, "密码和确认密码不匹配"),
    NEW_PASSWORD_CANNOT_BE_EMPTY(1208, "新密码不能为空"),
    NEW_PASSWORD_SAME_AS_CURRENT_PASSWORD(1209, "新密码不能与当前密码相同"),
    CURRENT_PASSWORD_CANNOT_BE_EMPTY(1210, "当前密码不能为空"),
    CURRENT_PASSWORD_NOT_MATCH(1211, "当前密码不匹配"),
    PASSWORD_UPDATE_FAILED(1212, "密码更新失败"),
    DATA_CANNOT_BE_EMPTY(1301, "数据不能为空"),
    ADMIN_OPERATION_FORBIDDEN(1302, "超级管理员用户禁止操作"),
    VERIFICATION_CODE_ERROR(1303, "验证码错误"),
    VERIFICATION_CODE_CANNOT_BE_EMPTY(1304, "验证码不能为空");

    private final int code;

    private final String message;
}
