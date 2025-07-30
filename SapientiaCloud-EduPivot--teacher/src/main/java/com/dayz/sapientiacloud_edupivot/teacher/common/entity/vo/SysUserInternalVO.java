package com.dayz.sapientiacloud_edupivot.teacher.common.entity.vo;

import com.dayz.sapientiacloud_edupivot.teacher.common.entity.po.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统用户内部数据传输对象 (VO)")
public class SysUserInternalVO extends SysUser implements Serializable {

    @Serial
    private static final long serialVersionUID = -9073375615020477662L;

    @Schema(name = "roles", description = "用户角色列表")
    private List<SysRoleVO> roles;

    @Schema(name = "permissions", description = "用户权限列表")
    private List<SysPermissionVO> permissions;
}
