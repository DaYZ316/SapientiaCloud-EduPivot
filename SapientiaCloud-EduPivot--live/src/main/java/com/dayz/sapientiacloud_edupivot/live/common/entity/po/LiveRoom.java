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
@TableName("mg_course_record")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "直播房间持久化对象（映射 mg_course_record 表中的直播相关字段）")
public class LiveRoom extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private UUID id;

    @TableField("live_room_name")
    private String roomName;

    @TableField("teacher_id")
    private UUID teacherId;

    @TableField("course_id")
    private UUID courseId;

    @TableField("live_status")
    private Integer status;

    @TableField("live_lk_room_name")
    private String lkRoomName;

    @TableField("live_lk_room_sid")
    private String lkRoomSid;

    @TableField("live_lk_node_id")
    private String lkNodeId;

    @TableField("live_start_time")
    private LocalDateTime startTime;

    @TableField("live_expected_end_time")
    private LocalDateTime expectedEndTime;

    @TableField("live_end_time")
    private LocalDateTime endTime;

    @TableField("live_max_participants")
    private Integer maxParticipants;

    @TableField("live_recording_enabled")
    private Integer recordingEnabled;

    @TableField("live_egress_task_id")
    private String egressTaskId;

    @TableField("live_egress_status")
    private Integer egressStatus;

    @TableField("live_recording_asset_url")
    private String recordingAssetUrl;

    @TableField("live_stream_output_urls")
    private String streamOutputUrls;

    @TableField("live_metadata")
    private String metadata;
}