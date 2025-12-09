package com.dayz.sapientiacloud_edupivot.course.constant;

import com.dayz.sapientiacloud_edupivot.course.enums.DifficultyEnum;

public class QuestionConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_QUESTION_BANK_ID = "question_bank_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_QUESTION_TITLE = "question_title";
    public static final String FIELD_QUESTION_CONTENT = "question_content";
    public static final String FIELD_QUESTION_TYPE = "question_type";
    public static final String FIELD_DIFFICULTY = "difficulty";
    public static final String FIELD_SCORE = "score";
    public static final String FIELD_ESTIMATED_TIME = "estimated_time";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_IMAGE_URLS = "image_urls";
    public static final String FIELD_EXPLANATION = "explanation";
    public static final String FIELD_REFERENCE_ANSWER = "reference_answer";
    public static final String FIELD_IS_RANDOM = "is_random";
    public static final String FIELD_VIEW_COUNT = "view_count";
    public static final String FIELD_CELESTIAL_QUESTION_ID = "celestial_question_id";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 题目类型范围
    public static final int QUESTION_TYPE_SINGLE_CHOICE = 0;
    public static final int QUESTION_TYPE_MULTIPLE_CHOICE = 1;
    public static final int QUESTION_TYPE_TRUE_FALSE = 2;
    public static final int QUESTION_TYPE_FILL_BLANK = 3;
    public static final int QUESTION_TYPE_SHORT_ANSWER = 4;
    public static final int QUESTION_TYPE_MIN = QUESTION_TYPE_SINGLE_CHOICE;
    public static final int QUESTION_TYPE_MAX = QUESTION_TYPE_SHORT_ANSWER;

    // 难度等级范围
    public static final int DIFFICULTY_MIN = DifficultyEnum.EASY.getCode();
    public static final int DIFFICULTY_MAX = DifficultyEnum.HARD.getCode();

    // 随机选项范围
    public static final int IS_RANDOM_FIXED = 0;
    public static final int IS_RANDOM_RANDOM = 1;
    public static final int IS_RANDOM_MIN = IS_RANDOM_FIXED;
    public static final int IS_RANDOM_MAX = IS_RANDOM_RANDOM;

    // 默认值
    public static final int DEFAULT_QUESTION_TYPE = QUESTION_TYPE_SINGLE_CHOICE;
    public static final int DEFAULT_DIFFICULTY = DifficultyEnum.EASY.getCode();
    public static final int DEFAULT_ESTIMATED_TIME = 5;
    public static final int DEFAULT_IS_RANDOM = IS_RANDOM_FIXED;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 时间格式
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加题目: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新题目: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除题目: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除题目完成，成功删除 {} 个题目";
    public static final String LOG_PUBLISH_SUCCESS = "成功发布题目: {}";
    public static final String LOG_UNPUBLISH_SUCCESS = "成功取消发布题目: {}";
    public static final String LOG_DELETE_FAILED = "删除题目失败: {}, 错误: {}";
    public static final String LOG_QUERY_SUCCESS = "题目查询成功: {}";
    public static final String LOG_STATISTICS_SUCCESS = "题目统计成功: {}";
    public static final String LOG_IMPORT_SUCCESS = "题目导入成功: {}";
    public static final String LOG_EXPORT_SUCCESS = "题目导出成功: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // 统计相关
    public static final String STATS_TOTAL_COUNT = "totalCount";
    public static final String STATS_SINGLE_CHOICE_COUNT = "singleChoiceCount";
    public static final String STATS_MULTIPLE_CHOICE_COUNT = "multipleChoiceCount";
    public static final String STATS_TRUE_FALSE_COUNT = "trueFalseCount";
    public static final String STATS_FILL_BLANK_COUNT = "fillBlankCount";
    public static final String STATS_SHORT_ANSWER_COUNT = "shortAnswerCount";
    public static final String STATS_EASY_COUNT = "easyCount";
    public static final String STATS_MEDIUM_COUNT = "mediumCount";
    public static final String STATS_HARD_COUNT = "hardCount";
    public static final String STATS_AVERAGE_SCORE = "averageScore";
    public static final String STATS_TOTAL_SCORE = "totalScore";
    public static final String STATS_AVERAGE_TIME = "averageTime";
    public static final String STATS_TOTAL_TIME = "totalTime";

    // 题目类型名称
    public static final String QUESTION_TYPE_NAME_SINGLE_CHOICE = "单选题";
    public static final String QUESTION_TYPE_NAME_MULTIPLE_CHOICE = "多选题";
    public static final String QUESTION_TYPE_NAME_TRUE_FALSE = "判断题";
    public static final String QUESTION_TYPE_NAME_FILL_BLANK = "填空题";
    public static final String QUESTION_TYPE_NAME_SHORT_ANSWER = "简答题";

    // 难度等级名称
    public static final String DIFFICULTY_NAME_EASY = "简单";
    public static final String DIFFICULTY_NAME_MEDIUM = "中等";
    public static final String DIFFICULTY_NAME_HARD = "困难";

    // 随机选项名称
    public static final String IS_RANDOM_NAME_FIXED = "固定顺序";
    public static final String IS_RANDOM_NAME_RANDOM = "随机顺序";

    // 文件相关
    public static final String IMAGE_EXTENSIONS = "jpg,jpeg,png,gif,bmp,webp";
    public static final String MAX_IMAGE_SIZE = "5MB";
    public static final String MAX_IMAGE_COUNT = "10";

    // 验证相关
    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 100;
    public static final int MIN_ESTIMATED_TIME = 1;
    public static final int MAX_ESTIMATED_TIME = 120;

    private QuestionConstants() {
        // 禁止实例化
    }
}
