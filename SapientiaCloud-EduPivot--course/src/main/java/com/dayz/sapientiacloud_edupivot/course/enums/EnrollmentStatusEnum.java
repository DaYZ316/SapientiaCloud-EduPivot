package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnrollmentStatusEnum implements BaseEnum {

    ENROLLED(0, "在读"),
    WITHDRAWN(1, "已退课"),
    COMPLETED(2, "已完成");

    private final int code;

    private final String message;
}
