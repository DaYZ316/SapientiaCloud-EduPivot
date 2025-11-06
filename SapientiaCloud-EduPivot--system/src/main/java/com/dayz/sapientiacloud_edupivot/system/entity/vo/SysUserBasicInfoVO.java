package com.dayz.sapientiacloud_edupivot.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "用户基本信息（脱敏）")
public class SysUserBasicInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8879095778150237771L;

    @Schema(name = "id", description = "用户ID")
    private UUID id;

    @Schema(name = "username", description = "用户名")
    private String username;

    @Schema(name = "nickName", description = "用户昵称")
    private String nickName;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

