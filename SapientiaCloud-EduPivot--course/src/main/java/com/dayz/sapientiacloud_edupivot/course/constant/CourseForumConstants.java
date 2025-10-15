package com.dayz.sapientiacloud_edupivot.course.constant;

public class CourseForumConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_FORUM_NAME = "forum_name";
    public static final String FIELD_FORUM_TYPE = "forum_type";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_POST_COUNT = "post_count";
    public static final String FIELD_REPLY_COUNT = "reply_count";
    public static final String FIELD_CREATED_TIME = "created_time";
    public static final String FIELD_UPDATED_TIME = "updated_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 状态值
    public static final int STATUS_MIN = 0;
    public static final int STATUS_MAX = 2;

    // 默认值
    public static final int DEFAULT_SORT_ORDER = 0;
    public static final long DEFAULT_POST_COUNT = 0L;
    public static final long DEFAULT_REPLY_COUNT = 0L;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加论坛: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新论坛: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除论坛: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除论坛完成，成功删除 {} 个论坛";
    public static final String LOG_UPDATE_STATUS_SUCCESS = "成功更新论坛状态: {} -> {}";
    public static final String LOG_BATCH_UPDATE_SORT_SUCCESS = "批量更新论坛排序完成，共更新 {} 个论坛";
    public static final String LOG_DELETE_FAILED = "删除论坛失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    private CourseForumConstants() {
        // 禁止实例化
    }
}
