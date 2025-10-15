package com.dayz.sapientiacloud_edupivot.student.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.student.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcademicStatusEnum implements BaseEnum {

    STUDYING(0, "在读"),
    SUSPENSION(1, "休学"),
    WITHDRAWAL(2, "退学"),
    GRADUATED(3, "毕业");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= STUDYING.code && code <= GRADUATED.code;
    }
}