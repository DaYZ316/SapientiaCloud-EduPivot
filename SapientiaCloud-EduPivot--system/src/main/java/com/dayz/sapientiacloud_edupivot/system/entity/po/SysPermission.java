package com.dayz.sapientiacloud_edupivot.system.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dayz.sapientiacloud_edupivot.system.entity.base.BaseEntity;
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
@TableName("sys_permission")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统权限持久化对象 (PO)")
public class SysPermission extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 8437350580466345040L;

    @Schema(name = "id", description = "权限ID")
    @TableId(value = "id")
    private UUID id;

    @Schema(name = "parent_id", description = "父级权限ID")
    @TableField("parent_id")
    private UUID parentId;

    @Schema(name = "permission_name", description = "权限名称")
    @TableField("permission_name")
    private String permissionName;

    @Schema(name = "permission_key", description = "权限标识")
    @TableField("permission_key")
    private String permissionKey;

    @Schema(name = "sort", description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    @TableField("status")
    private Integer status;
} 