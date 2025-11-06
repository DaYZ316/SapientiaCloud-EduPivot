package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContentTypeEnum implements BaseEnum {

    CHAPTER(0, "章节内容"),
    QUESTION(1, "问题"),
    ANSWER(2, "答案"),
    FORUM(3, "论坛帖子");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && code >= CHAPTER.code && code <= FORUM.code;
    }
}

