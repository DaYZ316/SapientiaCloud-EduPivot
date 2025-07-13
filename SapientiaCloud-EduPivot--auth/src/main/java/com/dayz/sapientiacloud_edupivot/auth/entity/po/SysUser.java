package com.dayz.sapientiacloud_edupivot.auth.entity.po;

import com.dayz.sapientiacloud_edupivot.auth.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户持久化对象 (PO)")
public class SysUser extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 726580191133270732L;

    @Schema(name = "id", description = "用户ID")
    private UUID id;

    @Schema(name = "username", description = "用户名")
    private String username;

    @Schema(name = "password", description = "密码", hidden = true)
    private String password;

    @Schema(name = "nick_name", description = "用户昵称")
    private String nickName;

    @Schema(name = "email", description = "邮箱")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    private String mobile;

    @Schema(name = "gender", description = "性别 (0=未知, 1=男, 2=女)")
    private Integer gender;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "last_login_time", description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}