package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskStatusEnum implements BaseEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "发布"),
    IN_PROGRESS(2, "进行中"),
    ENDED(3, "已结束");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= DRAFT.code && code <= ENDED.code;
    }
}
