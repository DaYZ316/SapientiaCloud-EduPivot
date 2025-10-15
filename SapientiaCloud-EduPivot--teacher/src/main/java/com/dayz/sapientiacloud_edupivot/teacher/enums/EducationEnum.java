package com.dayz.sapientiacloud_edupivot.teacher.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EducationEnum implements BaseEnum {

    COLLEGE(0, "专科"),
    BACHELOR(1, "本科"),
    MASTER(2, "硕士"),
    DOCTOR(3, "博士");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= COLLEGE.code && code <= DOCTOR.code;
    }
}