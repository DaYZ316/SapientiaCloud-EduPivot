package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileStatusEnum implements BaseEnum {

    NORMAL(0, "正常"),
    DELETED(1, "已删除"),
    PROCESSING(2, "处理中"),
    FAILED(3, "处理失败");

    private final int code;
    private final String message;

    public static FileStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

