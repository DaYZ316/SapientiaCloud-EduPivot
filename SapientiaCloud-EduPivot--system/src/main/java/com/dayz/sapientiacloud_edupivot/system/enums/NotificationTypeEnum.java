package com.dayz.sapientiacloud_edupivot.system.enums;

import com.dayz.sapientiacloud_edupivot.system.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum NotificationTypeEnum implements BaseEnum {

    SYSTEM(0, "系统通知"),
    COURSE(1, "课程通知"),
    TASK(2, "作业通知"),
    LIVE(3, "直播通知"),
    OTHER(4, "其他");

    private final int code;

    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= SYSTEM.code && code <= OTHER.code;
    }

    public static NotificationTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }
}

