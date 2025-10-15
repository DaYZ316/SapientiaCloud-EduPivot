package com.dayz.sapientiacloud_edupivot.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnrollmentStatusEnum implements BaseEnum {

    ENROLLED(0, "在读"),
    WITHDRAWN(1, "已退课"),
    COMPLETED(2, "已完成");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == ENROLLED.code || code == WITHDRAWN.code || code == COMPLETED.code);
    }
}
