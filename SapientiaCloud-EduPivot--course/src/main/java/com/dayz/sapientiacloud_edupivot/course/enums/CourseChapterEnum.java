package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseChapterEnum implements BaseEnum {

    // 通用验证错误
    CHAPTER_REQUIRED(50001, "章节不能为空"),
    CHAPTER_ID_REQUIRED(50002, "章节ID不能为空"),
    CHAPTER_INFO_REQUIRED(50003, "章节信息不能为空"),
    CHAPTER_INFO_OR_ID_REQUIRED(50004, "章节信息或ID不能为空"),
    CHAPTER_ID_LIST_REQUIRED(50005, "章节ID列表不能为空"),
    CHAPTER_NOT_EXISTS(50006, "章节不存在"),
    CHAPTER_NAME_EXISTS(50007, "章节名称已存在"),
    CHAPTER_TITLE_REQUIRED(50008, "章节标题不能为空"),
    CHAPTER_SORT_REQUIRED(50009, "章节排序不能为空"),
    CHAPTER_SORT_INVALID(50010, "章节排序必须大于0"),

    // 课程相关错误
    COURSE_ID_REQUIRED(50011, "课程ID不能为空"),
    COURSE_NOT_EXISTS(50012, "课程不存在"),
    COURSE_NOT_AVAILABLE(50013, "课程不可用"),

    // 父章节相关错误
    PARENT_CHAPTER_NOT_EXISTS(50014, "父章节不存在"),
    PARENT_CHAPTER_INVALID(50015, "父章节无效"),
    CANNOT_SET_SELF_AS_PARENT(50016, "不能将自己设置为父章节"),
    CANNOT_SET_CHILD_AS_PARENT(50017, "不能将子章节设置为父章节"),

    // 章节状态相关错误
    CHAPTER_STATUS_INVALID(50018, "章节状态值无效 (0=正常, 1=停用)"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(50019, "页码不能为空"),
    PAGE_SIZE_REQUIRED(50020, "页面大小不能为空"),

    // 操作相关错误
    CHAPTER_ADD_FAILED(50021, "添加章节失败"),
    CHAPTER_UPDATE_FAILED(50022, "更新章节失败"),
    CHAPTER_DELETE_FAILED(50023, "删除章节失败"),
    CHAPTER_QUERY_FAILED(50024, "查询章节失败"),
    CHAPTER_TREE_BUILD_FAILED(50025, "构建章节树失败");

    private final int code;

    private final String message;
}
