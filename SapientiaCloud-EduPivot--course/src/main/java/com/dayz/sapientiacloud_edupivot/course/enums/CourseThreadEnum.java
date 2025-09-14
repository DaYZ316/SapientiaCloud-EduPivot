package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseThreadEnum implements BaseEnum {

    // 通用验证错误
    THREAD_REQUIRED(60001, "主贴不能为空"),
    THREAD_ID_REQUIRED(60002, "主贴ID不能为空"),
    THREAD_INFO_REQUIRED(60003, "主贴信息不能为空"),
    THREAD_INFO_OR_ID_REQUIRED(60004, "主贴信息或ID不能为空"),
    THREAD_ID_LIST_REQUIRED(60005, "主贴ID列表不能为空"),
    THREAD_NOT_EXISTS(60006, "主贴不存在"),
    THREAD_TITLE_REQUIRED(60007, "主贴标题不能为空"),
    THREAD_CONTENT_REQUIRED(60008, "主贴内容不能为空"),

    // 课程相关错误
    COURSE_ID_REQUIRED(60009, "课程ID不能为空"),
    COURSE_NOT_EXISTS(60010, "课程不存在"),
    COURSE_NOT_AVAILABLE(60011, "课程不可用"),

    // 用户相关错误
    USER_ID_REQUIRED(60012, "用户ID不能为空"),
    USER_NOT_EXISTS(60013, "用户不存在"),

    // 主贴状态相关错误
    THREAD_STATUS_INVALID(60014, "主贴状态值无效 (0=正常, 1=关闭)"),
    THREAD_IS_CLOSED(60015, "主贴已关闭，无法操作"),
    THREAD_IS_PINNED(60016, "主贴已置顶"),
    THREAD_NOT_PINNED(60017, "主贴未置顶"),

    // 权限相关错误
    NO_PERMISSION_TO_EDIT(60018, "没有编辑权限"),
    NO_PERMISSION_TO_DELETE(60019, "没有删除权限"),
    NO_PERMISSION_TO_PIN(60020, "没有置顶权限"),
    NO_PERMISSION_TO_CLOSE(60021, "没有关闭权限"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(60022, "页码不能为空"),
    PAGE_SIZE_REQUIRED(60023, "页面大小不能为空"),

    // 操作相关错误
    THREAD_ADD_FAILED(60024, "发布主贴失败"),
    THREAD_UPDATE_FAILED(60025, "更新主贴失败"),
    THREAD_DELETE_FAILED(60026, "删除主贴失败"),
    THREAD_QUERY_FAILED(60027, "查询主贴失败"),
    THREAD_PIN_FAILED(60028, "置顶主贴失败"),
    THREAD_UNPIN_FAILED(60029, "取消置顶失败"),
    THREAD_CLOSE_FAILED(60030, "关闭主贴失败"),
    THREAD_OPEN_FAILED(60031, "开启主贴失败"),
    THREAD_VIEW_COUNT_UPDATE_FAILED(60032, "更新浏览次数失败");

    private final int code;

    private final String message;
}
