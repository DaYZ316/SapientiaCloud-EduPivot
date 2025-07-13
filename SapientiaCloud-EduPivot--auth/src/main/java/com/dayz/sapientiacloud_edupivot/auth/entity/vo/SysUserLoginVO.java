package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "系统用户登录信息 (VO)")
public class SysUserLoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3412795250372765420L;

    @Schema(description = "访问令牌")
    private String accessToken;
    
    @Schema(description = "用户ID")
    private UUID userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "用户昵称")
    private String nickName;
}
