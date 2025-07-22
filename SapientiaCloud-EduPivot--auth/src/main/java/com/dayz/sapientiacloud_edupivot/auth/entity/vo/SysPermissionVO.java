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
@Schema(description = "系统权限视图对象 (VO)")
public class SysPermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -824868033585933864L;

    @Schema(name = "id", description = "权限ID")
    private UUID id;

    @Schema(name = "parentId", description = "父级权限ID")
    private UUID parentId;

    @Schema(name = "permissionName", description = "权限名称")
    private String permissionName;

    @Schema(name = "permissionKey", description = "权限标识")
    private String permissionKey;

    @Schema(name = "children", description = "子权限列表")
    private List<SysPermissionVO> children;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "status", description = "状态 (0=正常, 1=停用)")
    private Integer status;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 