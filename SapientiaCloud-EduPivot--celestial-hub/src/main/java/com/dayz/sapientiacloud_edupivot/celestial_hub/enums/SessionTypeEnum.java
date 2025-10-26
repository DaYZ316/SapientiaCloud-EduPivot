package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionTypeEnum implements BaseEnum {

    GENERAL(0, "普通对话"),
    COURSE_QA(1, "课程问答"),
    QUESTION_TUTOR(2, "题目辅导"),
    KNOWLEDGE_SEARCH(3, "知识检索");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= GENERAL.code && code <= KNOWLEDGE_SEARCH.code;
    }
}

