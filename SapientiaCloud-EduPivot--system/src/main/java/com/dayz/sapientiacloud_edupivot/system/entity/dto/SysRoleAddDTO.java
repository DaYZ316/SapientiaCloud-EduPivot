package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统角色数据传输对象 (DTO)")
public class SysRoleAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3106921865697892451L;

    @Schema(name = "roleName", description = "角色名称", example = "超级管理员")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @Schema(name = "roleKey", description = "角色标识", example = "ADMIN")
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 100, message = "角色标识长度不能超过100个字符")
    @Pattern(regexp = "^[A-Z]+$", message = "角色标识必须全部为大写字母")
    private String roleKey;

    @Schema(name = "sort", description = "排序", example = "1")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)", example = "0")
    private Integer status;

    @Schema(name = "description", description = "描述", example = "超级管理员角色")
    @Size(max = 500, message = "描述信息长度不能超过500个字符")
    private String description;
}
