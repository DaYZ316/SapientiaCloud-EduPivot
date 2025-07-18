package com.dayz.sapientiacloud_edupivot.auth.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统角色视图对象 (VO)")
public class SysRoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5337978323517901756L;

    @Schema(name = "id", description = "角色ID")
    private UUID id;

    @Schema(name = "role_name", description = "角色名称")
    private String roleName;

    @Schema(name = "role_key", description = "角色标识")
    private String roleKey;

    @Schema(name = "permissions", description = "权限列表")
    private List<SysPermissionVO> permissions;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "description", description = "描述")
    private String description;

    @Schema(name = "create_time", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "update_time", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 