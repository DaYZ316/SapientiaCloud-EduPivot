package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出勤状态枚举
 */
@Getter
@AllArgsConstructor
public enum AttendanceStatusEnum implements BaseEnum {


    NOT_SIGNED_IN(0, "未签到"),


    SIGNED_IN(1, "已签到"),

    ABSENT(2, "缺席");

    private final Integer code;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}