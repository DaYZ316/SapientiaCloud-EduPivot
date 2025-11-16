package com.dayz.sapientiacloud_edupivot.student.enums;

import com.dayz.sapientiacloud_edupivot.student.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StudentPracticeEnum implements BaseEnum {

    CLASSROOM_ID_REQUIRED(53001, "课堂ID不能为空"),
    QUESTION_ID_REQUIRED(53002, "题目ID不能为空"),
    ANSWER_REQUIRED(53003, "答案不能为空"),
    STUDENT_NOT_FOUND(53004, "学生信息不存在"),
    SUBMISSION_NOT_FOUND(53005, "作答记录不存在");

    private final int code;
    private final String message;
}