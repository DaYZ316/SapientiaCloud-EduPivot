package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SysUserProfileDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8892775579047819754L;

    @Schema(name = "username", description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4到20个字符之间")
    private String username;

    @Schema(name = "nickName", description = "用户昵称")
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
    // TODO: 验证码

    @Schema(name = "gender", description = "性别 (0=未知, 1=男, 2=女)")
    @Min(value = 0, message = "性别输入不正确")
    @Max(value = 2, message = "性别输入不正确")
    private Integer gender;

    @Schema(name = "avatar", description = "头像")
    private String avatar;
}
