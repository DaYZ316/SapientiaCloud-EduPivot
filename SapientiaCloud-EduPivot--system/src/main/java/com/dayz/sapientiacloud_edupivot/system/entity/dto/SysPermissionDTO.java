package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "系统权限数据传输对象 (DTO)")
public class SysPermissionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2936909544491159082L;

    @Schema(description = "权限ID")
    @NotNull(message = "权限ID不能为空")
    private UUID id;

    @Schema(description = "父级权限ID")
    private UUID parentId;

    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    @Schema(description = "权限标识")
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permissionKey;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 (0=正常, 1=停用)")
    private Integer status;
} 