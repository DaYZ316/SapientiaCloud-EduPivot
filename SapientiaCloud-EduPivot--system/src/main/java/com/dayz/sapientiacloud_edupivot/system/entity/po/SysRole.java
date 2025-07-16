package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.system.common.entity.BaseEntity;
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
@TableName("sys_role")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统角色持久化对象 (PO)")
public class SysRole extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4759063611439691423L;

    public static final String ADMIN_ROLE_KEY = "ADMIN";

    @Schema(name = "id", description = "角色ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "role_name", description = "角色名称")
    @TableField("role_name")
    private String roleName;

    @Schema(name = "role_key", description = "角色标识")
    @TableField("role_key")
    private String roleKey;

    @Schema(name = "sort", description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    @TableField("status")
    private Integer status;

    @Schema(name = "description", description = "描述")
    @TableField("description")
    private String description;

    public Boolean isAdmin() {
        return ADMIN_ROLE_KEY.equals(this.roleKey);
    }
} 