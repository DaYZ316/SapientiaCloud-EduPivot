package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PinnedEnum implements BaseEnum {

    NOT_PINNED(0, "未置顶"),
    PINNED(1, "已置顶");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;
}
