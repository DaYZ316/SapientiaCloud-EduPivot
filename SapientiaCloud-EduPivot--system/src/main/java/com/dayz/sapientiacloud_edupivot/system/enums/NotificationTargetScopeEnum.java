package com.dayz.sapientiacloud_edupivot.system.enums;

import com.dayz.sapientiacloud_edupivot.system.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationTargetScopeEnum implements BaseEnum {

    ROLE(0, "按角色发送"),
    COURSE_STUDENT(1, "按课程学生发送");

    private final int code;

    private final String message;

    public static boolean isValidCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (NotificationTargetScopeEnum value : values()) {
            if (value.code == code) {
                return true;
            }
        }
        return false;
    }
}


