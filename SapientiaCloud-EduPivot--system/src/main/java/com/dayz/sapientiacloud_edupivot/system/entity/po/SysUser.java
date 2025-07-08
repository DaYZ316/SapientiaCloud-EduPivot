package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.system.entity.base.BaseEntity;
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
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户持久化对象 (PO)")
public class SysUser extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 726580191133270732L;

    @Schema(description = "用户ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码", hidden = true)
    @TableField("password")
    private String password;

    @Schema(description = "用户昵称")
    @TableField("nick_name")
    private String nickName;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("mobile")
    private String mobile;

    @Schema(description = "性别 (0=未知, 1=男, 2=女)")
    @TableField("gender")
    private Integer gender;

    @Schema(description = "用户头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "状态 (0=正常, 1=停用)")
    @TableField("status")
    private Integer status;

    @Schema(description = "最后登录时间")
    @TableField("last_login_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}