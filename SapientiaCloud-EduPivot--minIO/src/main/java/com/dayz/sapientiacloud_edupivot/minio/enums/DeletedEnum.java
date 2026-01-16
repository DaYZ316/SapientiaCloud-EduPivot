package com.dayz.sapientiacloud_edupivot.minio.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeletedEnum implements BaseEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private final int code;

    @JsonValue
    private final String message;
}