package com.dayz.sapientiacloud_edupivot.course.constant;

import com.dayz.sapientiacloud_edupivot.course.enums.DifficultyEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.PublicStatusEnum;

public class CourseQuestionBankConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_BANK_NAME = "bank_name";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_BANK_TYPE = "bank_type";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_DIFFICULTY = "difficulty";
    public static final String FIELD_IS_PUBLIC = "is_public";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 题库类型范围
    public static final int BANK_TYPE_PRACTICE = 0;
    public static final int BANK_TYPE_EXAM = 1;
    public static final int BANK_TYPE_HOMEWORK = 2;
    public static final int BANK_TYPE_MIN = BANK_TYPE_PRACTICE;
    public static final int BANK_TYPE_MAX = BANK_TYPE_HOMEWORK;

    // 难度等级范围
    public static final int DIFFICULTY_MIN = DifficultyEnum.EASY.getCode();
    public static final int DIFFICULTY_MAX = DifficultyEnum.HARD.getCode();

    // 公开状态范围
    public static final int IS_PUBLIC_MIN = PublicStatusEnum.COURSE_MEMBERS_ONLY.getCode();
    public static final int IS_PUBLIC_MAX = PublicStatusEnum.PUBLIC.getCode();

    // 默认值
    public static final int DEFAULT_BANK_TYPE = BANK_TYPE_PRACTICE;
    public static final int DEFAULT_DIFFICULTY = DifficultyEnum.EASY.getCode();
    public static final int DEFAULT_IS_PUBLIC = PublicStatusEnum.COURSE_MEMBERS_ONLY.getCode();
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 时间格式
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加题库: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新题库: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除题库: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除题库完成，成功删除 {} 个题库";
    public static final String LOG_PUBLISH_SUCCESS = "成功发布题库: {}";
    public static final String LOG_UNPUBLISH_SUCCESS = "成功取消发布题库: {}";
    public static final String LOG_DELETE_FAILED = "删除题库失败: {}, 错误: {}";
    public static final String LOG_QUERY_SUCCESS = "题库查询成功: {}";
    public static final String LOG_STATISTICS_SUCCESS = "题库统计成功: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // 统计相关
    public static final String STATS_TOTAL_COUNT = "totalCount";
    public static final String STATS_PUBLIC_COUNT = "publicCount";
    public static final String STATS_PRIVATE_COUNT = "privateCount";
    public static final String STATS_PRACTICE_COUNT = "practiceCount";
    public static final String STATS_EXAM_COUNT = "examCount";
    public static final String STATS_HOMEWORK_COUNT = "homeworkCount";
    public static final String STATS_EASY_COUNT = "easyCount";
    public static final String STATS_MEDIUM_COUNT = "mediumCount";
    public static final String STATS_HARD_COUNT = "hardCount";
    public static final String STATS_AVERAGE_DIFFICULTY = "averageDifficulty";
    public static final String STATS_TOTAL_QUESTIONS = "totalQuestions";

    // 题库类型名称
    public static final String BANK_TYPE_NAME_PRACTICE = "练习题库";
    public static final String BANK_TYPE_NAME_EXAM = "考试题库";
    public static final String BANK_TYPE_NAME_HOMEWORK = "作业题库";

    // 难度等级名称
    public static final String DIFFICULTY_NAME_EASY = "简单";
    public static final String DIFFICULTY_NAME_MEDIUM = "中等";
    public static final String DIFFICULTY_NAME_HARD = "困难";

    // 公开状态名称
    public static final String IS_PUBLIC_NAME_PRIVATE = "私有";
    public static final String IS_PUBLIC_NAME_PUBLIC = "公开";

    private CourseQuestionBankConstants() {
        // 禁止实例化
    }
}
