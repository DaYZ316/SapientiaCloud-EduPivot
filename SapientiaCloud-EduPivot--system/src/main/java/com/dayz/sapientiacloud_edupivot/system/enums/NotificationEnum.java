package com.dayz.sapientiacloud_edupivot.system.enums;

import com.dayz.sapientiacloud_edupivot.system.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationEnum implements BaseEnum {

    NOTIFICATION_ID_REQUIRED(20001, "通知ID不能为空"),
    NOTIFICATION_NOT_EXISTS(20002, "通知不存在"),
    USER_ID_REQUIRED(20003, "用户ID不能为空"),
    TITLE_REQUIRED(20004, "通知标题不能为空"),
    CONTENT_REQUIRED(20005, "通知内容不能为空"),
    NOTIFICATION_TYPE_INVALID(20006, "通知类型无效"),
    NOTIFICATION_STATUS_INVALID(20007, "通知状态无效"),
    BATCH_MARK_READ_FAILED(20008, "批量标记已读失败"),
    NOTIFICATION_IDS_REQUIRED(20009, "通知ID列表不能为空"),
    DATA_CANNOT_BE_EMPTY(20010, "数据不能为空"),
    TARGET_SCOPE_INVALID(20011, "发送范围类型无效"),
    ROLE_KEY_REQUIRED(20012, "角色标识不能为空"),
    ROLE_NOT_EXISTS(20013, "角色不存在"),
    COURSE_ID_REQUIRED(20014, "课程ID不能为空"),
    TARGET_USER_NOT_FOUND(20015, "未找到任何目标用户");

    private final int code;

    private final String message;
}

