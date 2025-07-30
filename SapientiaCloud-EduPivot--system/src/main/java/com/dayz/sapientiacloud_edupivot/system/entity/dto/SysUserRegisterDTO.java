package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "用户注册请求的数据模型")
public class SysUserRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8030183384771991467L;

    @Schema(name = "username", description = "用户名，必须是4-20位的字母、数字或下划线", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4到20个字符之间")
    private String username;

    @Schema(name = "password", description = "用户密码，必须是6-20位的任意字符", example = "MyP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    @Schema(name = "confirmPassword", description = "确认密码，必须与密码字段一致", example = "MyP@ssw0rd123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(name = "avatar", description = "用户头像URL")
    private String avatar;

    @Schema(name = "nickName", description = "用户昵称")
    @Size(max = 30, message = "用户昵称不能超过30个字符")
    private String nickName;

//    @Schema(name = "mobile", description = "手机号码，用于接收短信验证码", example = "13812345678")
//    @NotBlank(message = "验证码不能为空")
//    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
//    private String mobile;

//    @Schema(name = "verificationCode", description = "注册验证码（短信或邮件验证码）", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotBlank(message = "验证码不能为空")
//    @Size(min = 6, max = 6, message = "验证码必须为6位")
//    private String verificationCode;
}