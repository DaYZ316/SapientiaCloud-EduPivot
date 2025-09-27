package com.dayz.sapientiacloud_edupivot.course.constant;

public class ForumReplyConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_POST_ID = "post_id";
    public static final String FIELD_FORUM_ID = "forum_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_PARENT_REPLY_ID = "parent_reply_id";
    public static final String FIELD_REPLY_TO_USER_ID = "reply_to_user_id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_LIKE_COUNT = "like_count";
    public static final String FIELD_REPLY_COUNT = "reply_count";
    public static final String FIELD_IS_ACCEPTED = "is_accepted";
    public static final String FIELD_IS_ANONYMOUS = "is_anonymous";
    public static final String FIELD_FLOOR_NUMBER = "floor_number";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 状态值
    public static final int STATUS_MIN = 0;
    public static final int STATUS_MAX = 3;

    // 默认值
    public static final long DEFAULT_LIKE_COUNT = 0L;
    public static final long DEFAULT_REPLY_COUNT = 0L;
    public static final int DEFAULT_IS_ACCEPTED = 0;
    public static final int DEFAULT_IS_ANONYMOUS = 0;
    public static final int DEFAULT_FLOOR_NUMBER = 1;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;

    // 回复层级限制
    public static final int MAX_REPLY_DEPTH = 5;

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加回复: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新回复: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除回复: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除回复完成，成功删除 {} 个回复";
    public static final String LOG_UPDATE_STATUS_SUCCESS = "成功更新回复状态: {} -> {}";
    public static final String LOG_LIKE_SUCCESS = "回复点赞成功: {}";
    public static final String LOG_UNLIKE_SUCCESS = "回复取消点赞成功: {}";
    public static final String LOG_ACCEPT_SUCCESS = "回复采纳成功: {}";
    public static final String LOG_UNACCEPT_SUCCESS = "回复取消采纳成功: {}";
    public static final String LOG_DELETE_FAILED = "删除回复失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    private ForumReplyConstants() {
        // 禁止实例化
    }
}
