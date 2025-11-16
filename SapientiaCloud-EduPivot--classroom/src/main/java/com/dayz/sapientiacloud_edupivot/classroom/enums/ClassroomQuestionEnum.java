package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClassroomQuestionEnum implements BaseEnum {

    CLASSROOM_ID_REQUIRED(52001, "课堂ID不能为空"),
    QUESTION_ID_REQUIRED(52002, "题目ID不能为空"),
    RECORD_NOT_EXISTS(52003, "课堂题目发布记录不存在"),
    DUPLICATE_QUESTION_IN_CLASSROOM(52004, "该题目已在课堂中发布"),
    START_TIME_REQUIRED(52005, "开始时间不能为空"),
    END_TIME_BEFORE_START(52006, "截止时间不能早于开始时间"),
    SCORE_INVALID(52007, "题目分值必须在0-100之间"),
    REQUIRED_FLAG_INVALID(52008, "是否必答标识无效");

    private final int code;
    private final String message;
}