package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 课堂题目状态枚举
 */
@Getter
@AllArgsConstructor
public enum ClassroomQuestionStatusEnum implements BaseEnum {

    /**
     * 待作答
     */
    WAITING_TO_ANSWER(0, "待作答"),

    /**
     * 待批阅
     */
    WAITING_TO_REVIEW(1, "待批阅"),

    /**
     * 已批阅
     */
    REVIEWED(2, "已批阅");

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

