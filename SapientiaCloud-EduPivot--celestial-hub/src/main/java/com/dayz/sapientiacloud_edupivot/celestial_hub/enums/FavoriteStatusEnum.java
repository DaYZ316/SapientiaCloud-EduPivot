package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FavoriteStatusEnum implements BaseEnum {

    NOT_FAVORITE(0, "未收藏"),
    FAVORITE(1, "已收藏");

    @EnumValue
    private final int code;

    @JsonValue
    private final String message;

    public static boolean isValidCode(Integer code) {
        return code != null && (code == NOT_FAVORITE.code || code == FAVORITE.code);
    }
}

