package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "系统权限添加数据传输对象 (DTO)")
public class SysPermissionAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -526964902282253884L;

    @Schema(name = "parentId", description = "父级权限ID")
    private UUID parentId;

    @Schema(name = "permissionName", description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    @Schema(name = "permissionKey", description = "权限标识")
    @NotBlank(message = "权限标识不能为空")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permissionKey;

    @Schema(name = "sort", description = "排序")
    private Integer sort;
}
