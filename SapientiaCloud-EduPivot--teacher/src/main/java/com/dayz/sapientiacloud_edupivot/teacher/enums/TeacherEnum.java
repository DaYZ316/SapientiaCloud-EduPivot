package com.dayz.sapientiacloud_edupivot.teacher.enums;

import com.dayz.sapientiacloud_edupivot.teacher.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TeacherEnum implements BaseEnum {

    TEACHER_NOT_FOUND(50001, "教师信息不存在"),
    TEACHER_CODE_EXISTS(50002, "教师工号已存在"),
    TEACHER_CODE_REQUIRED(50003, "教师工号不能为空"),
    TEACHER_NAME_REQUIRED(50004, "教师姓名不能为空"),
    TEACHER_STATUS_INVALID(50005, "教师状态无效"),
    EDUCATION_INVALID(50006, "学历状态无效"),
    SYS_USER_NOT_BOUND(50007, "教师未绑定系统用户"),
    SYS_USER_ALREADY_BOUND(50008, "系统用户已绑定其他教师"),
    TEACHER_ID_REQUIRED(50009, "教师ID不能为空");

    private final Integer code;
    private final String message;
}