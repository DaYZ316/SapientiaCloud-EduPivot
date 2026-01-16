package com.dayz.sapientiacloud_edupivot.minio.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderEnum implements BaseEnum {

    UNKNOWN(0, "未知"),
    MALE(1, "男性"),
    FEMALE(2, "女性");

    private final int code;

    @JsonValue
    private final String message;

    public static boolean isCodeBetween(Integer code) {
        return code >= GenderEnum.UNKNOWN.code && code <= GenderEnum.FEMALE.code;
    }
}