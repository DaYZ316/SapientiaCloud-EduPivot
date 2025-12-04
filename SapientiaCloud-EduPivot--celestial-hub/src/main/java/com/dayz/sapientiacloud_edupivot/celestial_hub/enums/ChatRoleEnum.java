package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聊天角色枚举
 */
@Getter
@AllArgsConstructor
public enum ChatRoleEnum implements BaseEnum {

    USER(0, "用户"),
    ASSISTANT(1, "AI助手"),
    SYSTEM(2, "系统"),
    /**
     * 出题请求者（用户）
     */
    QUESTION_REQUESTER(3, "出题请求者"),
    /**
     * 出题者（AI）
     */
    QUESTION_GENERATOR(4, "出题者");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static ChatRoleEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ChatRoleEnum role : values()) {
            if (role.getCode() == code) {
                return role;
            }
        }
        return null;
    }
}

