package com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseQuestionBankEnum implements BaseEnum {

    COURSE_QUESTION_BANK_REQUIRED(70001, "题库信息不能为空"),
    COURSE_QUESTION_BANK_ID_REQUIRED(70002, "题库ID不能为空"),
    COURSE_QUESTION_BANK_IDS_REQUIRED(70003, "题库ID列表不能为空"),
    COURSE_QUESTION_BANK_NOT_EXISTS(70004, "题库不存在"),
    COURSE_QUESTION_BANK_ALREADY_EXISTS(70005, "题库已存在"),
    COURSE_QUESTION_BANK_NAME_REQUIRED(70006, "题库名称不能为空"),
    COURSE_QUESTION_BANK_TYPE_REQUIRED(70007, "题库类型不能为空"),
    COURSE_QUESTION_BANK_DIFFICULTY_REQUIRED(70008, "难度等级不能为空"),
    COURSE_QUESTION_BANK_IS_PUBLIC_REQUIRED(70009, "是否公开不能为空"),
    COURSE_ID_REQUIRED(70010, "课程ID不能为空"),
    COURSE_NOT_EXISTS(70011, "课程不存在"),
    COURSE_QUESTION_BANK_HAS_QUESTIONS(70012, "题库中还有题目，无法删除"),
    COURSE_QUESTION_BANK_ALREADY_PUBLISHED(70013, "题库已发布"),
    COURSE_QUESTION_BANK_NOT_PUBLISHED(70014, "题库未发布"),
    COURSE_QUESTION_BANK_INFO_OR_ID_REQUIRED(70015, "题库信息或ID不能为空");

    private final int code;
    private final String message;
}
