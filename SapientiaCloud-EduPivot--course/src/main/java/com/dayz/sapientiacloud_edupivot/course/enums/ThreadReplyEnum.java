package com.dayz.sapientiacloud_edupivot.course.enums;

import com.dayz.sapientiacloud_edupivot.course.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ThreadReplyEnum implements BaseEnum {

    // 通用验证错误
    REPLY_REQUIRED(70001, "回复不能为空"),
    REPLY_ID_REQUIRED(70002, "回复ID不能为空"),
    REPLY_INFO_REQUIRED(70003, "回复信息不能为空"),
    REPLY_INFO_OR_ID_REQUIRED(70004, "回复信息或ID不能为空"),
    REPLY_ID_LIST_REQUIRED(70005, "回复ID列表不能为空"),
    REPLY_NOT_EXISTS(70006, "回复不存在"),
    REPLY_CONTENT_REQUIRED(70007, "回复内容不能为空"),

    // 主贴相关错误
    THREAD_ID_REQUIRED(70008, "主贴ID不能为空"),
    THREAD_NOT_EXISTS(70009, "主贴不存在"),
    THREAD_IS_CLOSED(70010, "主贴已关闭，无法回复"),

    // 用户相关错误
    USER_ID_REQUIRED(70011, "用户ID不能为空"),
    USER_NOT_EXISTS(70012, "用户不存在"),

    // 父回复相关错误
    PARENT_REPLY_NOT_EXISTS(70013, "父回复不存在"),
    PARENT_REPLY_INVALID(70014, "父回复无效"),
    CANNOT_REPLY_TO_SELF(70015, "不能回复自己"),
    CANNOT_REPLY_TO_CHILD(70016, "不能回复子回复"),

    // 回复状态相关错误
    REPLY_STATUS_INVALID(70017, "回复状态值无效 (0=正常, 1=删除)"),

    // 权限相关错误
    NO_PERMISSION_TO_EDIT(70018, "没有编辑权限"),
    NO_PERMISSION_TO_DELETE(70019, "没有删除权限"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(70020, "页码不能为空"),
    PAGE_SIZE_REQUIRED(70021, "页面大小不能为空"),

    // 操作相关错误
    REPLY_ADD_FAILED(70022, "添加回复失败"),
    REPLY_UPDATE_FAILED(70023, "更新回复失败"),
    REPLY_DELETE_FAILED(70024, "删除回复失败"),
    REPLY_QUERY_FAILED(70025, "查询回复失败"),
    REPLY_TREE_BUILD_FAILED(70026, "构建回复树失败"),
    REPLY_COUNT_UPDATE_FAILED(70027, "更新回复数失败");

    private final int code;

    private final String message;
}
