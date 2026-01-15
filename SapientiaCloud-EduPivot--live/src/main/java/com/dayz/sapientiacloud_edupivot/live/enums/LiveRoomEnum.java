package com.dayz.sapientiacloud_edupivot.live.enums;

import com.dayz.sapientiacloud_edupivot.live.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 直播房间业务错误码
 */
@Getter
@AllArgsConstructor
public enum LiveRoomEnum implements BaseEnum {

    CLASSROOM_ID_REQUIRED(41001, "课堂记录ID不能为空"),
    CLASSROOM_NOT_EXISTS(41002, "课堂记录不存在"),
    ROOM_NOT_EXISTS(41003, "房间不存在"),

    RECORDING_ALREADY_RUNNING(41004, "当前已存在录制任务"),
    RECORDING_NOT_ENABLED(41005, "该房间未开启录制"),
    ROOM_NOT_LIVE(41006, "房间未直播中，无法录制"),
    RECORDING_NOT_RUNNING(41007, "当前没有进行中的录制任务"),
    RECORDING_ENV_DISABLED(41008, "当前环境未开启录制功能"),

    TEACHER_SERVICE_ERROR(41009, "教师信息不存在或教师服务异常"),
    STUDENT_SERVICE_ERROR(41010, "学生信息不存在或学生服务异常"),
    ROOM_NOT_LIVE_FOR_STUDENT(41011, "当前未开播，无法加入直播"),

    // 消息相关错误
    LIVE_ROOM_ID_REQUIRED(41012, "直播房间ID不能为空"),
    SENDER_ID_REQUIRED(41013, "发送者ID不能为空"),
    MESSAGE_CONTENT_REQUIRED(41014, "消息内容不能为空");

    private final int code;

    private final String message;
}


