package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "系统用户数据传输对象")
public class SysUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2283034824406690559L;

    @Schema(name = "id", description = "用户ID，更新时必须提供")
    @NotNull(message = "用户ID不能为空")
    private UUID id;

    @Schema(name = "nick_name", description = "用户昵称")
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称不能超过30个字符")
    private String nickName;

    @Schema(name = "email", description = "邮箱")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(name = "mobile", description = "手机号")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号码格式不正确")
    private String mobile;

    @Schema(name = "gender", description = "性别 (0=未知, 1=男, 2=女)")
    @Min(value = 0, message = "性别输入不正确")
    @Max(value = 2, message = "性别输入不正确")
    private Integer gender;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    @Min(value = 0, message = "状态输入不正确")
    @Max(value = 1, message = "状态输入不正确")
    private Integer status;

    @Schema(name = "last_login_time", description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}