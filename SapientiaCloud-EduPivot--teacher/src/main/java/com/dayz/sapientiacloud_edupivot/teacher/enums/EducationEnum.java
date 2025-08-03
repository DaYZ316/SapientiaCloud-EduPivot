package com.dayz.sapientiacloud_edupivot.teacher.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EducationEnum {

    COLLEGE(0, "专科"),
    BACHELOR(1, "本科"),
    MASTER(2, "硕士"),
    DOCTOR(3, "博士");

    private final Integer code;
    private final String desc;

    public static EducationEnum getByCode(Integer code) {
        for (EducationEnum education : values()) {
            if (education.getCode().equals(code)) {
                return education;
            }
        }
        return null;
    }
}