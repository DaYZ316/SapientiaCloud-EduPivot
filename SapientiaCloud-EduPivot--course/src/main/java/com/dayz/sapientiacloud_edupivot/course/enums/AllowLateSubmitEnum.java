package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AllowLateSubmitEnum implements BaseEnum {

    NOT_ALLOWED(0, "不允许"),
    ALLOWED(1, "允许");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == NOT_ALLOWED.code || code == ALLOWED.code);
    }
}
