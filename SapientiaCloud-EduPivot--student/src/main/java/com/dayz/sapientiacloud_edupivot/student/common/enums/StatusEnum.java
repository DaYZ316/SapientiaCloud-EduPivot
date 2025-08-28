package com.dayz.sapientiacloud_edupivot.student.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum implements BaseEnum {

    NORMAL(0, "正常"),
    DISABLED(1, "禁用");

    private final int code;

    private final String message;
}
