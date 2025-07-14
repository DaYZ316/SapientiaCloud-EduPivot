package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统角色查询对象 (DTO)")
public class SysRoleQueryDTO extends PageEntity {

    @Schema(name = "role_name", description = "角色名称")
    private String roleName;

    @Schema(name = "role_key", description = "角色标识")
    private String roleKey;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;
} 