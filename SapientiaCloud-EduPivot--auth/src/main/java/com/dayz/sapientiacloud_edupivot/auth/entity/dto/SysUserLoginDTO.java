package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "系统用户登录数据传输对象")
public class SysUserLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7940604844501036433L;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

}
