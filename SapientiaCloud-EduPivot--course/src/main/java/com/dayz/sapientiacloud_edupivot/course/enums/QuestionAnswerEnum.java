package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuestionAnswerEnum implements BaseEnum {

    QUESTION_ANSWER_REQUIRED(50001, "答案信息不能为空"),
    QUESTION_ANSWER_ID_REQUIRED(50002, "答案ID不能为空"),
    QUESTION_ANSWER_IDS_REQUIRED(50003, "答案ID列表不能为空"),
    QUESTION_ANSWER_NOT_EXISTS(50004, "答案不存在"),
    QUESTION_ANSWER_ALREADY_EXISTS(50005, "答案已存在"),
    QUESTION_ANSWER_CONTENT_REQUIRED(50006, "答案内容不能为空"),
    QUESTION_ANSWER_IS_CORRECT_REQUIRED(50007, "是否正确不能为空"),
    QUESTION_ANSWER_SCORE_REQUIRED(50008, "答案分数不能为空"),
    QUESTION_ID_REQUIRED(50009, "题目ID不能为空"),
    QUESTION_NOT_EXISTS(50010, "题目不存在"),
    SYS_USER_ID_REQUIRED(50011, "用户ID不能为空"),
    QUESTION_ANSWER_ALREADY_SUBMITTED(50012, "题目已提交答案"),
    QUESTION_ANSWER_AUTO_GRADE_FAILED(50013, "自动评分失败"),
    QUESTION_ANSWER_SCORE_INVALID(50014, "答案分数无效"),
    QUESTION_ANSWER_INFO_OR_ID_REQUIRED(50015, "答案信息或ID不能为空");

    private final int code;
    private final String message;
}
