package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ForumPostEnum implements BaseEnum {

    // 通用验证错误
    POST_REQUIRED(43001, "帖子不能为空"),
    POST_ID_REQUIRED(43002, "帖子ID不能为空"),
    POST_INFO_REQUIRED(43003, "帖子信息不能为空"),
    POST_INFO_OR_ID_REQUIRED(43004, "帖子信息或ID不能为空"),
    POST_ID_LIST_REQUIRED(43005, "帖子ID列表不能为空"),
    POST_NOT_EXISTS(43006, "帖子不存在"),
    POST_TITLE_REQUIRED(43007, "帖子标题不能为空"),
    POST_CONTENT_REQUIRED(43008, "帖子内容不能为空"),
    POST_STATUS_INVALID(43009, "帖子状态值无效 (0=正常, 1=停用)"),

    // 论坛相关错误
    FORUM_ID_REQUIRED(43010, "论坛ID不能为空"),
    FORUM_NOT_EXISTS(43011, "论坛不存在"),

    // 课程相关错误
    COURSE_ID_REQUIRED(43012, "课程ID不能为空"),
    COURSE_NOT_EXISTS(43013, "课程不存在"),

    // 作者相关错误
    AUTHOR_ID_REQUIRED(43014, "作者ID不能为空"),
    AUTHOR_NOT_EXISTS(43015, "作者不存在"),

    // 统计相关错误
    VIEW_COUNT_INVALID(43016, "浏览次数无效"),
    LIKE_COUNT_INVALID(43017, "点赞次数无效"),
    REPLY_COUNT_INVALID(43018, "回复次数无效"),
    SHARE_COUNT_INVALID(43019, "分享次数无效"),

    // 帖子属性相关错误
    IS_TOP_INVALID(43020, "置顶状态值无效 (0=否, 1=是)"),
    IS_ESSENCE_INVALID(43021, "精华状态值无效 (0=否, 1=是)"),
    IS_LOCKED_INVALID(43022, "锁定状态值无效 (0=否, 1=是)"),
    IS_ANONYMOUS_INVALID(43023, "匿名状态值无效 (0=否, 1=是)"),

    // 权限相关错误
    NO_PERMISSION_TO_POST(43024, "没有发帖权限"),
    NO_PERMISSION_TO_EDIT(43025, "没有编辑权限"),
    NO_PERMISSION_TO_DELETE(43026, "没有删除权限"),
    NO_PERMISSION_TO_TOP(43027, "没有置顶权限"),
    NO_PERMISSION_TO_ESSENCE(43028, "没有精华权限"),
    NO_PERMISSION_TO_LOCK(43029, "没有锁定权限"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(43030, "页码不能为空"),
    PAGE_SIZE_REQUIRED(43031, "页面大小不能为空");

    private final int code;

    private final String message;
}
