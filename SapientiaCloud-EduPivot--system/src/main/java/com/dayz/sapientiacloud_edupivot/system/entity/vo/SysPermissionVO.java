package com.dayz.sapientiacloud_edupivot.system.entity.vo;

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

    @Schema(name = "parent_id", description = "父级权限ID")
    private UUID parentId;

    @Schema(name = "permission_name", description = "权限名称")
    private String permissionName;

    @Schema(name = "permission_key", description = "权限标识")
    private String permissionKey;

    @Schema(name = "children", description = "子权限列表")
    private List<SysPermissionVO> children;

    @Schema(name = "sort", description = "排序")
    private Integer sort;

    @Schema(name = "create_time", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "update_time", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 