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
@TableName("sys_user_role")
@Schema(description = "用户角色关联对象 (PO)")
public class SysUserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(name = "user_id", description = "用户ID")
    @TableField("user_id")
    private UUID userId;

    @Schema(name = "role_id", description = "角色ID")
    @TableField("role_id")
    private UUID roleId;
} 