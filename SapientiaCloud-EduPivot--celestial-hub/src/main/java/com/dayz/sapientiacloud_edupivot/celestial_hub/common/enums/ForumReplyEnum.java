package com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ForumReplyEnum implements BaseEnum {

    // 通用验证错误
    REPLY_REQUIRED(44001, "回复不能为空"),
    REPLY_ID_REQUIRED(44002, "回复ID不能为空"),
    REPLY_INFO_REQUIRED(44003, "回复信息不能为空"),
    REPLY_INFO_OR_ID_REQUIRED(44004, "回复信息或ID不能为空"),
    REPLY_ID_LIST_REQUIRED(44005, "回复ID列表不能为空"),
    REPLY_NOT_EXISTS(44006, "回复不存在"),
    REPLY_CONTENT_REQUIRED(44007, "回复内容不能为空"),
    REPLY_STATUS_INVALID(44008, "回复状态值无效 (0=正常, 1=停用)"),

    // 帖子相关错误
    POST_ID_REQUIRED(44009, "帖子ID不能为空"),
    POST_NOT_EXISTS(44010, "帖子不存在"),

    // 论坛相关错误
    FORUM_ID_REQUIRED(44011, "论坛ID不能为空"),
    FORUM_NOT_EXISTS(44012, "论坛不存在"),

    // 课程相关错误
    COURSE_ID_REQUIRED(44013, "课程ID不能为空"),
    COURSE_NOT_EXISTS(44014, "课程不存在"),

    // 作者相关错误
    AUTHOR_ID_REQUIRED(44015, "作者ID不能为空"),
    AUTHOR_NOT_EXISTS(44016, "作者不存在"),

    // 父回复相关错误
    PARENT_REPLY_ID_REQUIRED(44017, "父回复ID不能为空"),
    PARENT_REPLY_NOT_EXISTS(44018, "父回复不存在"),

    // 统计相关错误
    LIKE_COUNT_INVALID(44019, "点赞次数无效"),
    REPLY_COUNT_INVALID(44020, "回复次数无效"),

    // 回复属性相关错误
    IS_ACCEPTED_INVALID(44021, "采纳状态值无效 (0=否, 1=是)"),
    IS_ANONYMOUS_INVALID(44022, "匿名状态值无效 (0=否, 1=是)"),

    // 权限相关错误
    NO_PERMISSION_TO_REPLY(44023, "没有回复权限"),
    NO_PERMISSION_TO_EDIT(44024, "没有编辑权限"),
    NO_PERMISSION_TO_DELETE(44025, "没有删除权限"),
    NO_PERMISSION_TO_ACCEPT(44026, "没有采纳权限"),

    // 回复限制相关错误
    POST_LOCKED(44027, "帖子已锁定，无法回复"),
    REPLY_TOO_DEEP(44028, "回复层级过深"),
    REPLY_TOO_FREQUENT(44029, "回复过于频繁，请稍后再试"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(44030, "页码不能为空"),
    PAGE_SIZE_REQUIRED(44031, "页面大小不能为空");

    private final int code;

    private final String message;
}
