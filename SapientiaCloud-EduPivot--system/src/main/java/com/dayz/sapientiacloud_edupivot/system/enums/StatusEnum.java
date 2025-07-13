package com.dayz.sapientiacloud_edupivot.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {

    /**
     * 正常
     */
    NORMAL(0, "正常"),
    /**
     * 禁用
     */
    DISABLED(1, "禁用");

    private final int code;

    private final String message;
}
