package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseForumEnum implements BaseEnum {

    // 通用验证错误
    FORUM_REQUIRED(42001, "论坛不能为空"),
    FORUM_ID_REQUIRED(42002, "论坛ID不能为空"),
    FORUM_INFO_REQUIRED(42003, "论坛信息不能为空"),
    FORUM_INFO_OR_ID_REQUIRED(42004, "论坛信息或ID不能为空"),
    FORUM_ID_LIST_REQUIRED(42005, "论坛ID列表不能为空"),
    FORUM_NOT_EXISTS(42006, "论坛不存在"),
    FORUM_NAME_EXISTS(42007, "论坛名称已存在"),
    FORUM_STATUS_INVALID(42008, "论坛状态值无效 (0=正常, 1=停用)"),

    // 课程相关错误
    COURSE_ID_REQUIRED(42009, "课程ID不能为空"),
    COURSE_NOT_EXISTS(42010, "课程不存在"),

    // 论坛类型相关错误
    FORUM_TYPE_REQUIRED(42011, "论坛类型不能为空"),
    FORUM_TYPE_INVALID(42012, "论坛类型无效"),

    // 排序相关错误
    SORT_ORDER_INVALID(42013, "排序值无效"),

    // 统计相关错误
    POST_COUNT_INVALID(42014, "帖子数量无效"),
    REPLY_COUNT_INVALID(42015, "回复数量无效"),

    // 最后帖子相关错误
    LAST_POST_ID_REQUIRED(42016, "最后帖子ID不能为空"),
    LAST_POST_TIME_REQUIRED(42017, "最后帖子时间不能为空"),

    // 版主相关错误
    MODERATOR_IDS_REQUIRED(42018, "版主ID列表不能为空"),
    MODERATOR_NOT_EXISTS(42019, "版主不存在"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(42020, "页码不能为空"),
    PAGE_SIZE_REQUIRED(42021, "页面大小不能为空");

    private final int code;

    private final String message;
}
