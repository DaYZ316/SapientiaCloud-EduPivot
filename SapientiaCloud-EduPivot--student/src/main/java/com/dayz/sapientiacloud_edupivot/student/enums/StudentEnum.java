package com.dayz.sapientiacloud_edupivot.student.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentEnum {

    STUDENT_NOT_FOUND(40001, "学生信息不存在"),
    STUDENT_CODE_EXISTS(40002, "学号已存在"),
    STUDENT_CODE_REQUIRED(40003, "学号不能为空"),
    STUDENT_NAME_REQUIRED(40004, "学生姓名不能为空"),
    STUDENT_STATUS_INVALID(40005, "学生状态无效"),
    ACADEMIC_STATUS_INVALID(40006, "学籍状态无效"),
    SYS_USER_NOT_BOUND(40007, "学生未绑定系统用户"),
    SYS_USER_ALREADY_BOUND(40008, "系统用户已绑定其他学生");

    private final Integer code;
    private final String message;
}