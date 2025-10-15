package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionOptionEnum implements BaseEnum {

    QUESTION_OPTION_REQUIRED(60001, "选项信息不能为空"),
    QUESTION_OPTION_ID_REQUIRED(60002, "选项ID不能为空"),
    QUESTION_OPTION_IDS_REQUIRED(60003, "选项ID列表不能为空"),
    QUESTION_OPTION_NOT_EXISTS(60004, "选项不存在"),
    QUESTION_OPTION_ALREADY_EXISTS(60005, "选项已存在"),
    QUESTION_OPTION_CONTENT_REQUIRED(60006, "选项内容不能为空"),
    QUESTION_OPTION_LABEL_REQUIRED(60007, "选项标签不能为空"),
    QUESTION_OPTION_IS_CORRECT_REQUIRED(60008, "是否正确答案不能为空"),
    QUESTION_ID_REQUIRED(60009, "题目ID不能为空"),
    QUESTION_NOT_EXISTS(60010, "题目不存在"),
    QUESTION_OPTION_LABEL_DUPLICATE(60011, "选项标签重复"),
    QUESTION_OPTION_LABEL_INVALID(60012, "选项标签无效"),
    QUESTION_OPTION_INFO_OR_ID_REQUIRED(60013, "选项信息或ID不能为空");

    private final int code;
    private final String message;
}
