package com.dayz.sapientiacloud_edupivot.student.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcademicStatusEnum {

    STUDYING(0, "在读"),
    SUSPENSION(1, "休学"),
    WITHDRAWAL(2, "退学"),
    GRADUATED(3, "毕业");

    private final Integer code;
    private final String desc;

    public static AcademicStatusEnum getByCode(Integer code) {
        for (AcademicStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}