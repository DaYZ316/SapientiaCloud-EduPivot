package com.dayz.sapientiacloud_edupivot.student.enums;

import com.dayz.sapientiacloud_edupivot.student.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcademicStatusEnum implements BaseEnum {

    STUDYING(0, "在读"),
    SUSPENSION(1, "休学"),
    WITHDRAWAL(2, "退学"),
    GRADUATED(3, "毕业");

    private final int code;
    private final String message;

    public static AcademicStatusEnum getByCode(Integer code) {
        for (AcademicStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}