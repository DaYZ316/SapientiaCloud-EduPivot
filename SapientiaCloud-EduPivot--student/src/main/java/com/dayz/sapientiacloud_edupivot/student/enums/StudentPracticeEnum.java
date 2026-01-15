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
    SUBMISSION_NOT_FOUND(53005, "作答记录不存在"),
    PRACTICE_ID_REQUIRED(53006, "练习ID不能为空"),
    STUDENT_ID_REQUIRED(53009, "学生ID不能为空"),
    COURSE_ID_REQUIRED(53007, "课程ID不能为空"),
    QUERY_DTO_REQUIRED(53008, "查询参数不能为空");

    private final int code;
    private final String message;
}