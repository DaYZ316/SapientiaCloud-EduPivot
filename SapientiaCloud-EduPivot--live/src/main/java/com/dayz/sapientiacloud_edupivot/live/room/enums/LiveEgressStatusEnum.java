package com.dayz.sapientiacloud_edupivot.live.room.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 直播录制任务状态
 */
@Getter
@AllArgsConstructor
public enum LiveEgressStatusEnum {

    /**
     * 未启动
     */
    IDLE(0, "未录制"),

    /**
     * 录制中
     */
    RUNNING(1, "录制中"),

    /**
     * 停止中
     */
    STOPPING(2, "停止中"),

    /**
     * 已停止
     */
    STOPPED(3, "已停止"),

    /**
     * 失败
     */
    FAILED(4, "录制失败");

    private final int code;
    private final String label;
}

