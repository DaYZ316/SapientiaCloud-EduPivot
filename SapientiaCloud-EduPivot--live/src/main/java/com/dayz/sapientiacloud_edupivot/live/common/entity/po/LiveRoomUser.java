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
@TableName("mg_live_room_user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "房间用户关联持久化对象")
public class LiveRoomUser extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private UUID id;

    @TableField("live_room_id")
    private UUID liveRoomId;

    @TableField("user_id")
    private UUID userId;

    @TableField("role")
    private Integer role;

    @TableField("join_time")
    private LocalDateTime joinTime;

    @TableField("leave_time")
    private LocalDateTime leaveTime;

    @TableField("lk_identity")
    private String lkIdentity;

    @TableField("lk_participant_sid")
    private String lkParticipantSid;

    @TableField("token_jti")
    private String tokenJti;

    @TableField("join_ip")
    private String joinIp;

    @TableField("client_platform")
    private String clientPlatform;

    @TableField("kicked_by")
    private UUID kickedBy;

    @TableField("kicked_at")
    private LocalDateTime kickedAt;

    @TableField("remark")
    private String remark;
}