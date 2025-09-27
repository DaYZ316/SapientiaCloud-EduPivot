package com.dayz.sapientiacloud_edupivot.course.constant;

public class CourseChapterConstants {

    // MongoDB 字段名称
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_CHAPTER_NAME = "chapter_name";
    public static final String FIELD_PARENT_CHAPTER_ID = "parent_chapter_id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_VIEW_COUNT = "view_count";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_SORT_ORDER = "sort_order";
    public static final String FIELD_CHAPTER_NUMBER = "chapter_number";
    public static final String FIELD_LIKE_COUNT = "like_count";
    public static final String FIELD_ID = "_id";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 状态值
    public static final int STATUS_MIN = 0;
    public static final int STATUS_MAX = 2;

    // 默认值
    public static final int DEFAULT_SORT_ORDER = 0;
    public static final long DEFAULT_VIEW_COUNT = 0L;
    public static final long DEFAULT_LIKE_COUNT = 0L;
    public static final long DEFAULT_COMMENT_COUNT = 0L;
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加章节: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新章节: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除章节: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除章节完成，成功删除 {} 个章节";
    public static final String LOG_UPDATE_STATUS_SUCCESS = "成功更新章节状态: {} -> {}";
    public static final String LOG_UPDATE_SORT_SUCCESS = "成功更新章节排序: {} -> {}";
    public static final String LOG_BATCH_UPDATE_SORT_SUCCESS = "批量更新章节排序完成，共更新 {} 个章节";
    public static final String LOG_LIKE_SUCCESS = "章节点赞成功: {}";
    public static final String LOG_UNLIKE_SUCCESS = "章节取消点赞成功: {}";
    public static final String LOG_VIEW_SUCCESS = "章节浏览成功: {}";
    public static final String LOG_DELETE_FAILED = "删除章节失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    private CourseChapterConstants() {
        // 禁止实例化
    }
}
