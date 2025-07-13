package com.dayz.sapientiacloud_edupivot.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统用户相关业务异常枚举
 *
 * @author YourName
 * @date 2025-07-03
 */
@Getter
@AllArgsConstructor
public enum SysUserExceptionEnum {

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1001, "用户不存在"),
    /**
     * 用户名或密码错误
     */
    USERNAME_OR_PASSWORD_ERROR(1002, "用户名或密码错误"),
    /**
     * 用户账号已被禁用
     */
    USER_ACCOUNT_DISABLED(1003, "用户账号已被禁用"),
    /**
     * 用户账号已锁定
     */
    USER_ACCOUNT_LOCKED(1004, "用户账号已被锁定"),
    /**
     * 用户账号已过期
     */
    USER_ACCOUNT_EXPIRED(1005, "用户账号已过期"),
    /**
     * 原密码不匹配
     */
    PASSWORD_INCORRECT(1006, "原密码不正确"),
    /**
     * 新密码不能与原密码相同
     */
    USERNAME_ALREADY_EXISTS(1101, "用户名已存在"),
    /**
     * 邮箱地址已被注册
     */
    EMAIL_ALREADY_EXISTS(1102, "邮箱地址已被注册"),
    /**
     * 手机号码已被注册
     */
    PHONE_NUMBER_ALREADY_EXISTS(1103, "手机号码已被注册"),
    /**
     * 用户名不能为空
     */
    USERNAME_CANNOT_BE_EMPTY(1104, "用户名不能为空"),
    /**
     * 密码不能为空
     */
    PASSWORD_CANNOT_BE_EMPTY(1105, "密码不能为空"),
    /**
     * 邮箱地址不能为空
     */
    INSUFFICIENT_PERMISSIONS(1201, "用户权限不足");

    private final int code;

    private final String message;
}
