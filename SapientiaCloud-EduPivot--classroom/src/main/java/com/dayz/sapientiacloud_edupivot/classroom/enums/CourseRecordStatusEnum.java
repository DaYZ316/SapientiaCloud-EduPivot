package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 课程记录状态枚举
 */
@Getter
@AllArgsConstructor
public enum CourseRecordStatusEnum implements BaseEnum {

    NOT_STARTED(0, "未开始"),

    IN_PROGRESS(1, "进行中"),


    ENDED(2, "已结束"),


    CANCELLED(3, "已取消");

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