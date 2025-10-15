package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DifficultyEnum implements BaseEnum {

    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == EASY.code || code == MEDIUM.code || code == HARD.code);
    }
}
