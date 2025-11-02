package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "绑定手机号请求的数据模型")
public class BindMobileDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4477631762355442283L;

    @Schema(name = "mobile", description = "手机号码", example = "13812345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String mobile;

    @Schema(name = "verificationCode", description = "手机验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须为6位")
    private String verificationCode;

    @Schema(name = "userId", description = "用户ID（可选，如果不传则从当前登录用户获取，或用于第三方登录场景）", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
}

