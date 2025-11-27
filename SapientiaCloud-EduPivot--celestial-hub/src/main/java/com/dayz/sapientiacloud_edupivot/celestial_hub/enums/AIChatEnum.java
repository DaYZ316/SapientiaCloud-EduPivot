package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AIChatEnum implements BaseEnum {

    // 会话相关错误
    SESSION_NOT_EXISTS(50001, "会话不存在"),
    SESSION_ID_REQUIRED(50002, "会话ID不能为空"),
    SESSION_USER_ID_REQUIRED(50003, "会话用户ID不能为空"),

    // 消息相关错误
    MESSAGE_NOT_EXISTS(50011, "消息不存在"),
    MESSAGE_ID_REQUIRED(50012, "消息ID不能为空"),
    MESSAGE_CONTENT_REQUIRED(50013, "消息内容不能为空"),
    MESSAGE_ROLE_INVALID(50014, "消息角色无效"),
    REQUEST_ID_REQUIRED(50015, "请求ID不能为空"),

    // AI服务相关错误
    AI_SERVICE_ERROR(50021, "AI服务异常，请稍后重试"),
    AI_MODEL_UNAVAILABLE(50022, "AI模型不可用"),
    AI_RESPONSE_TIMEOUT(50023, "AI响应超时"),

    // 反馈相关错误
    FEEDBACK_VALUE_INVALID(50031, "反馈值无效"),

    // RAG检索相关错误
    RAG_SEARCH_ERROR(50041, "知识检索失败");

    private final int code;

    private final String message;
}

