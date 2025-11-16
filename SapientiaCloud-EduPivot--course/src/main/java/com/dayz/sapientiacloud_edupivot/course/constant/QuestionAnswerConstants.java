package com.dayz.sapientiacloud_edupivot.course.constant;

import java.math.BigDecimal;

public final class QuestionAnswerConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_QUESTION_ID = "question_id";
    public static final String FIELD_ANSWER_CONTENT = "answer_content";
    public static final String FIELD_EXPLANATION = "explanation";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_SORT_ORDER = "sort_order";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
    public static final String SORT_BY_SORT_ORDER = FIELD_SORT_ORDER;

    // 分值相关
    public static final BigDecimal DEFAULT_SCORE = BigDecimal.ZERO;
    public static final BigDecimal MIN_SCORE = BigDecimal.ZERO;
    public static final BigDecimal MAX_SCORE = BigDecimal.valueOf(100);

    // 答案序号
    public static final int DEFAULT_SORT_ORDER = 1;

    private QuestionAnswerConstants() {
        // 禁止实例化
    }
}

