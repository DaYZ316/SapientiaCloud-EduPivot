package com.dayz.sapientiacloud_edupivot.live.enums;

import com.dayz.sapientiacloud_edupivot.live.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LiveRoomEnum implements BaseEnum {

    CLASSROOM_ID_REQUIRED(41001, "课堂记录 ID 不能为空"),
    CLASSROOM_NOT_EXISTS(41002, "课堂记录不存在"),
    ROOM_NOT_EXISTS(41003, "直播间不存在"),

    RECORDING_ALREADY_RUNNING(41004, "当前已存在录制任务"),
    RECORDING_NOT_ENABLED(41005, "该直播间未开启录制"),
    ROOM_NOT_LIVE(41006, "直播间未在直播中，无法录制"),
    RECORDING_NOT_RUNNING(41007, "当前没有进行中的录制任务"),
    RECORDING_ENV_DISABLED(41008, "当前环境未开启录制功能"),
    RECORDING_START_FAILED(41009, "直播已开启，但录制启动失败"),
    RECORDING_DELETE_FAILED(41020, "录制文件删除失败"),

    TEACHER_SERVICE_ERROR(41010, "教师信息不存在或教师服务异常"),
    STUDENT_SERVICE_ERROR(41011, "学生信息不存在或学生服务异常"),
    ROOM_NOT_LIVE_FOR_STUDENT(41012, "当前未开播，无法加入直播"),
    COURSE_RECORD_SERVICE_ERROR(41013, "课程记录信息不存在或课程记录服务异常"),
    LIVE_START_TOO_EARLY(41014, "开播只能在开课前 10 分钟内开始"),
    LIVE_WINDOW_EXPIRED(41015, "该课程的直播时间窗口已结束"),

    LIVE_ROOM_ID_REQUIRED(41016, "直播间 ID 不能为空"),
    SENDER_ID_REQUIRED(41017, "发送者 ID 不能为空"),
    MESSAGE_CONTENT_REQUIRED(41018, "消息内容不能为空"),
    LIVE_ROOM_ALREADY_JOINED(41019, "已在其他设备参与，无法加入直播"),
    LIVE_ROOM_SESSION_INVALID(41021, "直播会话已失效，请重新加入");

    private final int code;

    private final String message;
}
