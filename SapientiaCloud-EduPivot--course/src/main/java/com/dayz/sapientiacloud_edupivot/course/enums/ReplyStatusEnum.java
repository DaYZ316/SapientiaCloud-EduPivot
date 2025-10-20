package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReplyStatusEnum implements BaseEnum {

    NORMAL(0, "正常"),
    DELETED(1, "删除"),
    PENDING(2, "审核中"),
    REJECTED(3, "审核失败");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= NORMAL.code && code <= REJECTED.code;
    }
}
