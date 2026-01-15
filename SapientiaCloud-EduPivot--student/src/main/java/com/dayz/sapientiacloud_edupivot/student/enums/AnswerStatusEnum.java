package com.dayz.sapientiacloud_edupivot.student.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.student.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnswerStatusEnum implements BaseEnum {

    INCORRECT(0, "错误"),
    CORRECT(1, "正确"),
    PARTIALLY_CORRECT(2, "半对"),
    PENDING_REVIEW(3, "待批阅");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= INCORRECT.code && code <= PENDING_REVIEW.code;
    }
}
