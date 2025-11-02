package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "通过手机验证码修改密码请求的数据模型")
public class SysUserMobilePasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1234567890123456789L;

    @Schema(name = "mobile", description = "手机号码", example = "13812345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String mobile;

    @Schema(name = "verificationCode", description = "手机验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为6位")
    private String verificationCode;

    @Schema(name = "newPassword", description = "新密码", example = "newPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String newPassword;

    @Schema(name = "confirmPassword", description = "确认密码", example = "newPassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}

