package com.dayz.sapientiacloud_edupivot.course.constant;

import com.dayz.sapientiacloud_edupivot.course.enums.DifficultyEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.TaskStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.TaskTypeEnum;

public class CourseTaskConstants {

    // MongoDB 字段名称
    public static final String FIELD_ID = "_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_TASK_NAME = "task_name";
    public static final String FIELD_TASK_DESCRIPTION = "task_description";
    public static final String FIELD_TASK_TYPE = "task_type";
    public static final String FIELD_TASK_CONTENT = "task_content";
    public static final String FIELD_ATTACHMENT_URLS = "attachment_urls";
    public static final String FIELD_RESOURCE_URLS = "resource_urls";
    public static final String FIELD_MAX_SCORE = "max_score";
    public static final String FIELD_START_TIME = "start_time";
    public static final String FIELD_END_TIME = "end_time";
    public static final String FIELD_ALLOW_LATE_SUBMIT = "allow_late_submit";
    public static final String FIELD_MAX_SUBMIT_COUNT = "max_submit_count";
    public static final String FIELD_AUTO_GRADE = "auto_grade";
    public static final String FIELD_TAGS = "tags";
    public static final String FIELD_DIFFICULTY = "difficulty";
    public static final String FIELD_ESTIMATED_TIME = "estimated_time";
    public static final String FIELD_VIEW_COUNT = "view_count";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // 排序相关
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    // 状态值范围
    public static final int STATUS_MIN = TaskStatusEnum.DRAFT.getCode();
    public static final int STATUS_MAX = TaskStatusEnum.ENDED.getCode();
    public static final int TASK_TYPE_MIN = TaskTypeEnum.HOMEWORK.getCode();
    public static final int TASK_TYPE_MAX = TaskTypeEnum.EXPERIMENT.getCode();
    public static final int DIFFICULTY_MIN = DifficultyEnum.EASY.getCode();
    public static final int DIFFICULTY_MAX = DifficultyEnum.HARD.getCode();

    // 默认值
    public static final long DEFAULT_VIEW_COUNT = 0L;
    public static final int DEFAULT_ALLOW_LATE_SUBMIT = 0;
    public static final int DEFAULT_MAX_SUBMIT_COUNT = 0;
    public static final int DEFAULT_AUTO_GRADE = 0;
    public static final int DEFAULT_DIFFICULTY = DifficultyEnum.EASY.getCode();
    public static final int DEFAULT_ESTIMATED_TIME = 60;
    public static final int DEFAULT_STATUS = TaskStatusEnum.DRAFT.getCode();
    public static final int INCREMENT_VALUE = 1;
    public static final int DECREMENT_VALUE = -1;

    // 分页相关
    public static final int PAGE_NUM_OFFSET = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // 时间格式
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // 日志消息模板
    public static final String LOG_ADD_SUCCESS = "成功添加任务: {}";
    public static final String LOG_UPDATE_SUCCESS = "成功更新任务: {}";
    public static final String LOG_DELETE_SUCCESS = "成功删除任务: {}";
    public static final String LOG_BATCH_DELETE_SUCCESS = "批量删除任务完成，成功删除 {} 个任务";
    public static final String LOG_UPDATE_STATUS_SUCCESS = "成功更新任务状态: {} -> {}";
    public static final String LOG_PUBLISH_SUCCESS = "成功发布任务: {}";
    public static final String LOG_UNPUBLISH_SUCCESS = "成功取消发布任务: {}";
    public static final String LOG_START_SUCCESS = "成功开始任务: {}";
    public static final String LOG_END_SUCCESS = "成功结束任务: {}";
    public static final String LOG_VIEW_SUCCESS = "任务浏览成功: {}";
    public static final String LOG_DELETE_FAILED = "删除任务失败: {}, 错误: {}";

    // 正则表达式标志
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // 统计相关
    public static final String STATS_TOTAL_COUNT = "totalCount";
    public static final String STATS_PUBLISHED_COUNT = "publishedCount";
    public static final String STATS_DRAFT_COUNT = "draftCount";
    public static final String STATS_RUNNING_COUNT = "runningCount";
    public static final String STATS_ENDED_COUNT = "endedCount";
    public static final String STATS_AVERAGE_SCORE = "averageScore";
    public static final String STATS_TOTAL_VIEWS = "totalViews";

    private CourseTaskConstants() {
        // 禁止实例化
    }
}
