package com.dayz.sapientiacloud_edupivot.live.common.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.live.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@TableName("mg_course_record_student")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房间用户关联持久化对象（映射 mg_course_record_student 表中的直播相关字段）")
public class LiveRoomUser extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private UUID id;

    @TableField("record_id")
    private UUID liveRoomId;

    @TableField("course_id")
    private UUID courseId;

    @TableField("student_id")
    private UUID studentId;

    @TableField("teacher_id")
    private UUID teacherId;

    @TableField(exist = false)
    private Integer role;

    @TableField("live_join_time")
    private LocalDateTime joinTime;

    @TableField("live_leave_time")
    private LocalDateTime leaveTime;

    @TableField("live_lk_identity")
    private String lkIdentity;

    @TableField("live_lk_participant_sid")
    private String lkParticipantSid;

    @TableField("live_token_jti")
    private String tokenJti;

    @TableField("live_join_ip")
    private String joinIp;

    @TableField("live_client_platform")
    private String clientPlatform;

    @TableField(exist = false)
    private UUID kickedBy;

    @TableField("live_kicked_at")
    private LocalDateTime kickedAt;

    @TableField("live_remark")
    private String remark;
}