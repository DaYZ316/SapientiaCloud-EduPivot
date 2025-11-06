package com.dayz.sapientiacloud_edupivot.course.constant;

public class QuestionOptionConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_QUESTION_ID = "question_id";
    public static final String FIELD_OPTION_CONTENT = "option_content";
    public static final String FIELD_OPTION_LABEL = "option_label";
    public static final String FIELD_IS_CORRECT = "is_correct";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_IMAGE_URLS = "image_urls";
    public static final String FIELD_EXPLANATION = "explanation";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 选项正确性范围
    public static final int IS_CORRECT_WRONG = 0;
    public static final int IS_CORRECT_CORRECT = 1;
    public static final int IS_CORRECT_MIN = IS_CORRECT_WRONG;
    public static final int IS_CORRECT_MAX = IS_CORRECT_CORRECT;

    // 选项标签范围
    public static final String OPTION_LABEL_A = "A";
    public static final String OPTION_LABEL_B = "B";
    public static final String OPTION_LABEL_C = "C";
    public static final String OPTION_LABEL_D = "D";
    public static final String OPTION_LABEL_E = "E";
    public static final String OPTION_LABEL_F = "F";
    public static final String OPTION_LABEL_G = "G";
    public static final String OPTION_LABEL_H = "H";
    public static final String[] OPTION_LABELS = {OPTION_LABEL_A, OPTION_LABEL_B, OPTION_LABEL_C, OPTION_LABEL_D,
            OPTION_LABEL_E, OPTION_LABEL_F, OPTION_LABEL_G, OPTION_LABEL_H};

    // 默认值
    public static final int DEFAULT_IS_CORRECT = IS_CORRECT_WRONG;
    public static final String DEFAULT_OPTION_LABEL = OPTION_LABEL_A;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 时间格式
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加选项: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新选项: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除选项: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除选项完成，成功删除 {} 个选项";
    public static final String LOG_DELETE_FAILED = "删除选项失败: {}, 错误: {}";
    public static final String LOG_QUERY_SUCCESS = "选项查询成功: {}";
    public static final String LOG_STATISTICS_SUCCESS = "选项统计成功: {}";
    public static final String LOG_BATCH_ADD_SUCCESS = "批量添加选项成功: {}";
    public static final String LOG_ADD_FAILED = "添加选项失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // 统计相关
    public static final String STATS_TOTAL_COUNT = "totalCount";
    public static final String STATS_CORRECT_COUNT = "correctCount";
    public static final String STATS_WRONG_COUNT = "wrongCount";
    public static final String STATS_AVERAGE_SCORE = "averageScore";
    public static final String STATS_TOTAL_SCORE = "totalScore";
    public static final String STATS_CORRECT_RATE = "correctRate";

    // 选项正确性名称
    public static final String IS_CORRECT_NAME_WRONG = "错误";
    public static final String IS_CORRECT_NAME_CORRECT = "正确";

    // 文件相关
    public static final String IMAGE_EXTENSIONS = "jpg,jpeg,png,gif,bmp,webp";
    public static final String MAX_IMAGE_SIZE = "5MB";
    public static final String MAX_IMAGE_COUNT = "5";

    // 验证相关
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    public static final int MIN_OPTION_CONTENT_LENGTH = 1;
    public static final int MAX_OPTION_CONTENT_LENGTH = 2000;
    public static final int MIN_OPTION_LABEL_LENGTH = 1;
    public static final int MAX_OPTION_LABEL_LENGTH = 10;
    public static final int MIN_EXPLANATION_LENGTH = 0;
    public static final int MAX_EXPLANATION_LENGTH = 1000;

    // 选项数量限制
    public static final int MIN_OPTIONS_PER_QUESTION = 2;
    public static final int MAX_OPTIONS_PER_QUESTION = 8;
    public static final int DEFAULT_OPTIONS_PER_QUESTION = 4;

    // 缓存相关
    public static final String CACHE_PREFIX_OPTION = "question_option:";
    public static final String CACHE_PREFIX_QUESTION_OPTIONS = "question_options:";
    public static final int CACHE_EXPIRE_TIME = 3600;

    // 批量操作相关
    public static final int BATCH_SIZE = 50;
    public static final int MAX_BATCH_SIZE = 200;

    // 选项排序相关
    public static final String SORT_BY_LABEL = "option_label";
    public static final String SORT_BY_CREATE_TIME = "create_time";
    public static final String SORT_BY_UPDATE_TIME = "update_time";

    // 选项标签验证
    public static final String OPTION_LABEL_PATTERN = "^[A-H]$";
    public static final String OPTION_LABEL_ERROR_MESSAGE = "选项标签必须是A-H之间的单个字母";

    private QuestionOptionConstants() {
        // 禁止实例化
    }
}
