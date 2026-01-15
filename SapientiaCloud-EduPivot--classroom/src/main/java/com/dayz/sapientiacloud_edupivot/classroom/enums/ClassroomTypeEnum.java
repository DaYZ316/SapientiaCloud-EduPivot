package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClassroomTypeEnum implements BaseEnum {

    SMALL(0, "小型教室"),
    MEDIUM(1, "中型教室"),
    LARGE(2, "大型教室"),
    EXTRA_LARGE(3, "超大型教室");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= SMALL.code && code <= EXTRA_LARGE.code;
    }
}

