package com.dayz.sapientiacloud_edupivot.course.constant;

public class CourseChapterConstants {

    // MongoDB 字段名称
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_TEACHER_ID = "teacher_id";
    public static final String FIELD_CHAPTER_NAME = "chapter_name";
    public static final String FIELD_PARENT_CHAPTER_ID = "parent_chapter_id";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_VIEW_COUNT = "view_count";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_SORT_ORDER = "sort_order";
    public static final String FIELD_LIKE_COUNT = "like_count";
    public static final String FIELD_UPDATE_TIME = "update_time";
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

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    private CourseChapterConstants() {
        // 禁止实例化
    }
}
