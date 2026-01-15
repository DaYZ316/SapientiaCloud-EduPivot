package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 座位状态枚举
 */
@Getter
@AllArgsConstructor
public enum SeatStatusEnum implements BaseEnum {

    /**
     * 正常状态
     */
    NORMAL(0, "normal"),

    /**
     * 已预留
     */
    RESERVED(2, "reserved"),

    /**
     * 已占用
     */
    OCCUPIED(3, "occupied");

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

