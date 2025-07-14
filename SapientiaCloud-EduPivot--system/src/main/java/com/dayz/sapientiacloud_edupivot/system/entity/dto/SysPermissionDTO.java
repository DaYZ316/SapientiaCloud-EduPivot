package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统权限数据传输对象 (DTO)")
public class SysPermissionDTO {

    @Schema(name = "id", description = "权限ID")
    private UUID id;

    @Schema(name = "parent_id", description = "父级权限ID")
    private UUID parentId;

    @Schema(name = "permission_name", description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    @Schema(name = "permission_key", description = "权限标识")
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permissionKey;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;
} 