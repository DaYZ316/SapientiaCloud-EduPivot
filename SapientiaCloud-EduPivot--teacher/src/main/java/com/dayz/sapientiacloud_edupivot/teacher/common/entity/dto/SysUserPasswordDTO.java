package com.dayz.sapientiacloud_edupivot.teacher.common.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SysUserPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4469374967563143947L;

    @Schema(name = "currentPassword", description = "当前密码")
    @NotBlank(message = "密码不能为空")
    private String currentPassword;

    @Schema(name = "newPassword", description = "新密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String newPassword;

    @Schema(name = "confirmPassword", description = "确认密码")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
