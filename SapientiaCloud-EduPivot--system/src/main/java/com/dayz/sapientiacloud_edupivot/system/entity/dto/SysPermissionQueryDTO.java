package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统权限查询对象 (DTO)")
public class SysPermissionQueryDTO extends PageEntity {

    @Schema(name = "permission_name", description = "权限名称")
    private String permissionName;

    @Schema(name = "permission_key", description = "权限标识")
    private String permissionKey;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;
    
    @Schema(name = "parent_id", description = "父级权限ID")
    private UUID parentId;
} 