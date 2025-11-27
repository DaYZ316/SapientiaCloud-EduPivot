package com.dayz.sapientiacloud_edupivot.live.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.live.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@TableName("mg_live_room")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "直播房间持久化对象")
public class LiveRoom extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private UUID id;

    @TableField("room_name")
    private String roomName;

    @TableField("creator_id")
    private UUID creatorId;

    @TableField("course_id")
    private UUID courseId;

    @TableField("classroom_id")
    private UUID classroomId;

    @TableField("status")
    private Integer status;

    @TableField("lk_room_name")
    private String lkRoomName;

    @TableField("lk_room_sid")
    private String lkRoomSid;

    @TableField("lk_node_id")
    private String lkNodeId;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("expected_end_time")
    private LocalDateTime expectedEndTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("max_participants")
    private Integer maxParticipants;

    @TableField("recording_enabled")
    private Integer recordingEnabled;

    @TableField("egress_task_id")
    private String egressTaskId;

    @TableField("egress_status")
    private Integer egressStatus;

    @TableField("recording_asset_url")
    private String recordingAssetUrl;

    @TableField("stream_output_urls")
    private String streamOutputUrls;

    @TableField("metadata")
    private String metadata;
}