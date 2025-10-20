package com.dayz.sapientiacloud_edupivot.system.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "系统用户视图对象 (VO)")
public class SysUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8879095778150237770L;

    @Schema(name = "id", description = "用户ID")
    private UUID id;

    @Schema(name = "username", description = "用户名")
    private String username;

    @Schema(name = "nickName", description = "用户昵称")
    private String nickName;

    @Schema(name = "email", description = "邮箱")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    private String mobile;

    @Schema(description = "性别 (0=未知, 1=男, 2=女)")
    private Integer gender;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(name = "roles", description = "用户角色列表")
    private List<SysRoleVO> roles;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "lastLoginTime", description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}