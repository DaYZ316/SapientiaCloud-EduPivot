package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionEnum implements BaseEnum {

    QUESTION_REQUIRED(80001, "题目信息不能为空"),
    QUESTION_ID_REQUIRED(80002, "题目ID不能为空"),
    QUESTION_IDS_REQUIRED(80003, "题目ID列表不能为空"),
    QUESTION_NOT_EXISTS(80004, "题目不存在"),
    QUESTION_ALREADY_EXISTS(80005, "题目已存在"),
    QUESTION_TITLE_REQUIRED(80006, "题目标题不能为空"),
    QUESTION_CONTENT_REQUIRED(80007, "题目内容不能为空"),
    QUESTION_TYPE_REQUIRED(80008, "题目类型不能为空"),
    QUESTION_DIFFICULTY_REQUIRED(80009, "难度等级不能为空"),
    QUESTION_SCORE_REQUIRED(80010, "题目分数不能为空"),
    QUESTION_BANK_ID_REQUIRED(80011, "题库ID不能为空"),
    QUESTION_BANK_NOT_EXISTS(80012, "题库不存在"),
    QUESTION_ALREADY_PUBLISHED(80013, "题目已发布"),
    QUESTION_NOT_PUBLISHED(80014, "题目未发布"),
    QUESTION_HAS_ANSWERS(80015, "题目已有答案，无法删除"),
    QUESTION_TYPE_INVALID(80016, "题目类型无效"),
    QUESTION_DIFFICULTY_INVALID(80017, "难度等级无效"),
    QUESTION_INFO_OR_ID_REQUIRED(80018, "题目信息或题目ID不能为空"),
    QUESTION_OPTION_SAVE_FAILED(80019, "题目选项保存失败"),
    QUESTION_ANSWER_SAVE_FAILED(80020, "题目答案保存失败"),
    QUESTION_ID_SHOULD_BE_NULL_ON_ADD(80021, "新增题目时ID必须为空"),
    QUESTION_SYS_USER_ID_REQUIRED(80022, "创建用户ID不能为空"),
    QUESTION_OPTION_REQUIRED(80023, "题目选项不能为空"),
    QUESTION_OPTION_REQUIRED_CORRECT(80024, "题目选项不能为空，请填写正确的选项"),
    QUESTION_CELESTIAL_QUESTION_ID_EXISTS(80025, "该AI生成题目ID已存在");

    private final int code;
    private final String message;
}
