package com.dayz.sapientiacloud_edupivot.system.entity.dto;

import com.dayz.sapientiacloud_edupivot.system.common.entity.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统角色查询对象 (DTO)")
public class SysRoleQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -6194177273518861829L;

    @Schema(name = "startTime", description = "角色名称")
    private String roleName;

    @Schema(name = "roleKey", description = "角色标识")
    private String roleKey;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;
} 