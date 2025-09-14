package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClosedEnum implements BaseEnum {

    NOT_CLOSED(0, "未关闭"),
    CLOSED(1, "已关闭");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;
}
