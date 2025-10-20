package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AutoGradeEnum implements BaseEnum {

    MANUAL(0, "手动评分"),
    AUTO(1, "自动评分");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == MANUAL.code || code == AUTO.code);
    }

}
