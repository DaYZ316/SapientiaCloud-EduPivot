package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统角色数据传输对象 (DTO)")
public class SysRoleDTO {

    @Schema(name = "id", description = "角色ID")
    private UUID id;

    @Schema(name = "role_name", description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过30个字符")
    private String roleName;

    @Schema(name = "role_key", description = "角色标识")
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 100, message = "角色标识长度不能超过100个字符")
    private String roleKey;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "description", description = "描述")
    @Size(max = 500, message = "描述信息长度不能超过500个字符")
    private String description;

    @Schema(name = "permissionIds", description = "权限ID列表")
    private List<UUID> permissionIds;
} 