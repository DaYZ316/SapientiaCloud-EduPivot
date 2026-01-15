package com.dayz.sapientiacloud_edupivot.system.enums;

import com.dayz.sapientiacloud_edupivot.system.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum NotificationStatusEnum implements BaseEnum {

    UNREAD(0, "未读"),
    READ(1, "已读");

    private final int code;

    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == UNREAD.code || code == READ.code);
    }

    public static NotificationStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }
}

