package com.dayz.sapientiacloud_edupivot.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenderEnum {

    /**
     * 不暴露
     */
    UNKNOWN(0, "未知"),

    /**
     * 男性
     */
    MALE(1, "男性"),

    /**
     * 女性
     */
    FEMALE(2, "女性");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String desc;

    public static boolean isCodeBetween(Integer code) {
        if (code < GenderEnum.UNKNOWN.code || code > GenderEnum.FEMALE.code) {
            return false;
        }
        return true;
    }
}