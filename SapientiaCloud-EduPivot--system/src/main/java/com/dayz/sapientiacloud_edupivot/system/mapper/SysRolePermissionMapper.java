package com.dayz.sapientiacloud_edupivot.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysRolePermissionMapper {
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<UUID> getRolePermissionIds(UUID roleId);

    int addRolePermissions(@Param("roleId") UUID roleId, @Param("permissionIds") List<UUID> permissionIds);

    int removeRolePermissions(@Param("roleId") UUID roleId, @Param("permissionIds") List<UUID> permissionIds);
}
