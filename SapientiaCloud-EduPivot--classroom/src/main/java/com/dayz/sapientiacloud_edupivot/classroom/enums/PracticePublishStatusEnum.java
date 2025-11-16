package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PracticePublishStatusEnum implements BaseEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    CLOSED(2, "已关闭");

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