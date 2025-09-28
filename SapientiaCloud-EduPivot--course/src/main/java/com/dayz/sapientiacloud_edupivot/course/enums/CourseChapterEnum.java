package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseChapterEnum implements BaseEnum {

    // 通用验证错误
    CHAPTER_REQUIRED(41001, "章节不能为空"),
    CHAPTER_ID_REQUIRED(41002, "章节ID不能为空"),
    CHAPTER_INFO_REQUIRED(41003, "章节信息不能为空"),
    CHAPTER_INFO_OR_ID_REQUIRED(41004, "章节信息或ID不能为空"),
    CHAPTER_ID_LIST_REQUIRED(41005, "章节ID列表不能为空"),
    CHAPTER_NOT_EXISTS(41006, "章节不存在"),
    CHAPTER_NAME_EXISTS(41007, "章节名称已存在"),
    CHAPTER_STATUS_INVALID(41008, "章节状态值无效 (0=正常, 1=停用)"),

    // 课程相关错误
    COURSE_ID_REQUIRED(41009, "课程ID不能为空"),
    COURSE_NOT_EXISTS(41010, "课程不存在"),

    // 教师相关错误
    TEACHER_NOT_EXISTS(41021, "教师不存在"),

    // 父章节相关错误
    PARENT_CHAPTER_ID_REQUIRED(41011, "父章节ID不能为空"),
    PARENT_CHAPTER_NOT_EXISTS(41012, "父章节不存在"),
    CHAPTER_HAS_CHILDREN(41013, "章节下存在子章节，无法删除"),

    // 排序相关错误
    SORT_ORDER_INVALID(41014, "排序值无效"),

    // 统计相关错误
    VIEW_COUNT_INVALID(41015, "浏览次数无效"),
    LIKE_COUNT_INVALID(41016, "点赞次数无效"),
    COMMENT_COUNT_INVALID(41017, "评论次数无效"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(41018, "页码不能为空"),
    PAGE_SIZE_REQUIRED(41019, "页面大小不能为空"),
    CHAPTER_NAME_REQUIRED(41020, "章节名称不能为空");

    private final int code;

    private final String message;
}
