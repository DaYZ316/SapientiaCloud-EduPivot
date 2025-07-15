package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_permission")
@Schema(description = "角色权限关联对象 (PO)")
public class SysRolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 5002629267574017459L;

    @Schema(name = "role_id", description = "角色ID")
    @TableField("role_id")
    private UUID roleId;

    @Schema(name = "permission_id", description = "权限ID")
    @TableField("permission_id")
    private UUID permissionId;
} 