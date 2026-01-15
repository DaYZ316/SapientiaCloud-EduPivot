package com.dayz.sapientiacloud_edupivot.student.constant;

/**
 * 学生练习相关常量
 */
public class QuestionStudentConstants {

    // MongoDB 字段名
    public static final String FIELD_ID = "id";
    public static final String FIELD_CLASSROOM_ID = "classroom_id";
    public static final String FIELD_STUDENT_ID = "student_id";
    public static final String FIELD_QUESTION_ID = "question_id";
    public static final String FIELD_PRACTICE_ID = "practice_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_ANSWER = "answer";
    public static final String FIELD_IS_CORRECT = "is_correct";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";
    public static final String FIELD_IS_DELETED = "is_deleted";

    // 状态值
    public static final Integer STATUS_DELETED = 1;
    public static final Integer STATUS_NOT_DELETED = 0;

    // 正确性状态
    public static final Integer ANSWER_INCORRECT = 0;
    public static final Integer ANSWER_CORRECT = 1;
    public static final Integer ANSWER_PARTIALLY_CORRECT = 2;
    public static final Integer ANSWER_PENDING_REVIEW = 3;

    // 题目类型
    public static final Integer QUESTION_TYPE_SINGLE_CHOICE = 0;
    public static final Integer QUESTION_TYPE_MULTIPLE_CHOICE = 1;
    public static final Integer QUESTION_TYPE_TRUE_FALSE = 2;
    public static final Integer QUESTION_TYPE_FILL_BLANK = 3;
    public static final Integer QUESTION_TYPE_ESSAY = 4;

    // 选项相关
    public static final String OPTION_LABEL = "optionLabel";
    public static final String OPTION_IS_CORRECT = "isCorrect";
    public static final String OPTION_SCORE = "score";

    // 答案相关
    public static final String ANSWER_CONTENT = "answerContent";

    private QuestionStudentConstants() {
        // 禁止实例化
    }
}
