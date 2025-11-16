package com.dayz.sapientiacloud_edupivot.student.enums;

import com.dayz.sapientiacloud_edupivot.student.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PracticeSubmissionStatusEnum implements BaseEnum {

    UNSUBMITTED(0, "未提交"),
    SUBMITTED(1, "已提交"),
    REVIEWED(2, "已评阅");

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