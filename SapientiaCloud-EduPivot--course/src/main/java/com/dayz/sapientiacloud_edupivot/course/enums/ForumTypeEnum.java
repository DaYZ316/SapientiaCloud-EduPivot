package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ForumTypeEnum implements BaseEnum {

    DISCUSSION(0, "讨论区"),
    Q_AND_A(1, "问答区"),
    ASSIGNMENT(2, "作业区"),
    ANNOUNCEMENT(3, "公告区");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= DISCUSSION.code && code <= ANNOUNCEMENT.code;
    }
}
