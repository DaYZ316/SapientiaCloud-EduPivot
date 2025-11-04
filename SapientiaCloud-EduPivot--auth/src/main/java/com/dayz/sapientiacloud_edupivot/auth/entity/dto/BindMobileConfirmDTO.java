package com.dayz.sapientiacloud_edupivot.auth.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "绑定手机号确认请求的数据模型")
public class BindMobileConfirmDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4477631762355442284L;

    @Schema(name = "mobile", description = "手机号码", example = "13812345678", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String mobile;

    @Schema(name = "userId", description = "临时账户ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private UUID userId;

    @Schema(name = "isSameAccount", description = "是否为同一账户（true=是，false=不是）", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "确认标识不能为空")
    private Boolean isSameAccount;
}

