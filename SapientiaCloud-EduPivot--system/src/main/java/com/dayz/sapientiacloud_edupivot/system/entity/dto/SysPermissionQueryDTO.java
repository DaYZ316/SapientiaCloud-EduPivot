package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统权限查询对象 (DTO)")
public class SysPermissionQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6491504560605823212L;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限标识")
    private String permissionKey;

    @Schema(description = "状态 (0=正常, 1=停用)")
    private Integer status;
    
    @Schema(description = "父级权限ID")
    private UUID parentId;
} 