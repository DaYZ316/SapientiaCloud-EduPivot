package com.dayz.sapientiacloud_edupivot.course.constant;

public class ForumPostConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_FORUM_ID = "forum_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_POST_TYPE = "post_type";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_VIEW_COUNT = "view_count";
    public static final String FIELD_LIKE_COUNT = "like_count";
    public static final String FIELD_REPLY_COUNT = "reply_count";
    public static final String FIELD_SHARE_COUNT = "share_count";
    public static final String FIELD_IS_TOP = "is_top";
    public static final String FIELD_IS_ESSENCE = "is_essence";
    public static final String FIELD_IS_LOCKED = "is_locked";
    public static final String FIELD_IS_ANONYMOUS = "is_anonymous";
    public static final String FIELD_LAST_REPLY_ID = "last_reply_id";
    public static final String FIELD_LAST_REPLY_TIME = "last_reply_time";
    public static final String FIELD_LAST_REPLY_USER_ID = "last_reply_user_id";
    public static final String FIELD_CHAPTER_ID = "chapter_id";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 状态值
    public static final int STATUS_MIN = 0;
    public static final int STATUS_MAX = 3;
    // 默认值
    public static final long DEFAULT_VIEW_COUNT = 0L;
    public static final long DEFAULT_LIKE_COUNT = 0L;
    public static final long DEFAULT_REPLY_COUNT = 0L;
    public static final long DEFAULT_SHARE_COUNT = 0L;
    public static final int DEFAULT_IS_TOP = 0;
    public static final int DEFAULT_IS_ESSENCE = 0;
    public static final int DEFAULT_IS_LOCKED = 0;
    public static final int DEFAULT_IS_ANONYMOUS = 0;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加帖子: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新帖子: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除帖子: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除帖子完成，成功删除 {} 个帖子";
    public static final String LOG_UPDATE_STATUS_SUCCESS = "成功更新帖子状态: {} -> {}";
    public static final String LOG_SET_TOP_SUCCESS = "成功设置帖子置顶: {} -> {}";
    public static final String LOG_SET_ESSENCE_SUCCESS = "成功设置帖子精华: {} -> {}";
    public static final String LOG_SET_LOCK_SUCCESS = "成功设置帖子锁定: {} -> {}";
    public static final String LOG_LIKE_SUCCESS = "帖子点赞成功: {}";
    public static final String LOG_UNLIKE_SUCCESS = "帖子取消点赞成功: {}";
    public static final String LOG_SHARE_SUCCESS = "帖子分享成功: {}";
    public static final String LOG_VIEW_SUCCESS = "帖子浏览成功: {}";
    public static final String LOG_DELETE_FAILED = "删除帖子失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    private ForumPostConstants() {
        // 禁止实例化
    }
}
