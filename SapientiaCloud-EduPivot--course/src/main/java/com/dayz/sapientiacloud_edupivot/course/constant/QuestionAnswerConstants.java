package com.dayz.sapientiacloud_edupivot.course.constant;

public class QuestionAnswerConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_QUESTION_ID = "question_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_ANSWER_CONTENT = "answer_content";
    public static final String FIELD_ANSWER_TEXT = "answer_text";
    public static final String FIELD_IS_CORRECT = "is_correct";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 答案正确性范围
    public static final int IS_CORRECT_WRONG = 0; // 错误
    public static final int IS_CORRECT_CORRECT = 1; // 正确
    public static final int IS_CORRECT_PARTIAL = 2; // 部分正确
    public static final int IS_CORRECT_MIN = IS_CORRECT_WRONG;
    public static final int IS_CORRECT_MAX = IS_CORRECT_PARTIAL;

    // 默认值
    public static final int DEFAULT_IS_CORRECT = IS_CORRECT_WRONG;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 时间格式
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加答案: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新答案: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除答案: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除答案完成，成功删除 {} 个答案";
    public static final String LOG_DELETE_FAILED = "删除答案失败: {}, 错误: {}";
    public static final String LOG_QUERY_SUCCESS = "答案查询成功: {}";
    public static final String LOG_STATISTICS_SUCCESS = "答案统计成功: {}";
    public static final String LOG_AUTO_GRADE_SUCCESS = "自动评分成功: {}";
    public static final String LOG_AUTO_GRADE_FAILED = "自动评分失败: {}, 错误: {}";
    public static final String LOG_ADD_FAILED = "添加答案失败: {}, 错误: {}";
    public static final String LOG_BATCH_ADD_SUCCESS = "批量添加答案成功: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // 统计相关
    public static final String STATS_TOTAL_COUNT = "totalCount";
    public static final String STATS_CORRECT_COUNT = "correctCount";
    public static final String STATS_WRONG_COUNT = "wrongCount";
    public static final String STATS_PARTIAL_COUNT = "partialCount";
    public static final String STATS_AVERAGE_SCORE = "averageScore";
    public static final String STATS_TOTAL_SCORE = "totalScore";
    public static final String STATS_CORRECT_RATE = "correctRate";
    public static final String STATS_ACCURACY_RATE = "accuracyRate";

    // 答案正确性名称
    public static final String IS_CORRECT_NAME_WRONG = "错误";
    public static final String IS_CORRECT_NAME_CORRECT = "正确";
    public static final String IS_CORRECT_NAME_PARTIAL = "部分正确";

    // 自动评分相关
    public static final String AUTO_GRADE_SINGLE_CHOICE = "single_choice";
    public static final String AUTO_GRADE_MULTIPLE_CHOICE = "multiple_choice";
    public static final String AUTO_GRADE_TRUE_FALSE = "true_false";
    public static final String AUTO_GRADE_FILL_BLANK = "fill_blank";
    public static final String AUTO_GRADE_SHORT_ANSWER = "short_answer";

    // 评分规则
    public static final String SCORE_RULE_FULL = "full"; // 全对得分
    public static final String SCORE_RULE_PARTIAL = "partial"; // 部分得分
    public static final String SCORE_RULE_NONE = "none"; // 不得分

    // 验证相关
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    public static final int MIN_ANSWER_LENGTH = 1;
    public static final int MAX_ANSWER_LENGTH = 5000;

    // 缓存相关
    public static final String CACHE_PREFIX_ANSWER = "question_answer:";
    public static final String CACHE_PREFIX_USER_ANSWER = "user_answer:";
    public static final String CACHE_PREFIX_QUESTION_ANSWER = "question_answers:";
    public static final int CACHE_EXPIRE_TIME = 3600; // 1小时

    // 批量操作相关
    public static final int BATCH_SIZE = 100;
    public static final int MAX_BATCH_SIZE = 1000;

    private QuestionAnswerConstants() {
        // 禁止实例化
    }
}
