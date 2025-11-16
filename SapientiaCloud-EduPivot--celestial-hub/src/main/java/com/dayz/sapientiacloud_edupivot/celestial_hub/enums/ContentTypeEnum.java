package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTypeEnum implements BaseEnum {

    CHAPTER(0, "章节内容", "chapter"),
    QUESTION(1, "问题", "question"),
    TASK(2, "任务", "task"),
    FORUM(3, "论坛帖子", "forum"),
    CHAT(4, "对话内容", "chat");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    private final String vectorNamespace;

    public static boolean isValidCode(Integer code) {
        return fromCode(code) != null;
    }

    public static ContentTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ContentTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

