package com.dayz.sapientiacloud_edupivot.system.enums;

/**
 * 通知消息箱类型枚举：区分已接收（收件箱）与已发送（发件箱）
 */
public enum NotificationBoxTypeEnum {
    RECEIVED(0),
    SENT(1);

    private final int code;

    NotificationBoxTypeEnum(int code) {
        this.code = code;
    }

    public static NotificationBoxTypeEnum fromCode(int code) {
        for (NotificationBoxTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }
}


