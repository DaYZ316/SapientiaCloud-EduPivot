package com.dayz.sapientiacloud_edupivot.live.room.constant;

/**
 * 直播房间相关常量
 */
public final class LiveRoomConstants {

    private LiveRoomConstants() {
    }

    /**
     * 房间状态：未开始
     */
    public static final int STATUS_NOT_STARTED = 0;

    /**
     * 房间状态：直播中
     */
    public static final int STATUS_LIVING = 1;

    /**
     * 房间状态：已结束
     */
    public static final int STATUS_ENDED = 2;

    /**
     * 默认最大在线人数
     */
    public static final int DEFAULT_MAX_PARTICIPANTS = 500;

    /**
     * 录制开关：关闭
     */
    public static final int RECORDING_DISABLED = 0;

    /**
     * 录制开关：开启
     */
    public static final int RECORDING_ENABLED = 1;
}


