package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统角色数据传输对象 (DTO)")
public class SysRoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9134795432837478770L;

    public static final String ADMIN_ROLE_KEY = "ADMIN";

    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private UUID id;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @Schema(description = "角色标识")
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 100, message = "角色标识长度不能超过100个字符")
    @Pattern(regexp = "^[A-Z]+$", message = "角色标识必须全部为大写字母")
    private String roleKey;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(description = "描述")
    @Size(max = 500, message = "描述信息长度不能超过500个字符")
    private String description;

    public Boolean isAdmin() {
        return ADMIN_ROLE_KEY.equals(this.roleKey);
    }
} 