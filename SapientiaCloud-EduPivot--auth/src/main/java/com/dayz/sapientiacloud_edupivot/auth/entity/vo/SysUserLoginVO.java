package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserInternalDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户登录信息 (VO)")
public class SysUserLoginVO extends SysUserInternalDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3412795250372765420L;

    @Schema(description = "访问令牌")
    private String accessToken;
}
