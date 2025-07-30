package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysRolePermissionMapper {

    List<SysPermissionVO> getRolePermissions(UUID roleId);

    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<UUID> getRolePermissionIds(UUID roleId);

    int addRolePermissions(@Param("roleId") UUID roleId, @Param("permissionIds") List<UUID> permissionIds);

    int removeRolePermissions(@Param("roleId") UUID roleId, @Param("permissionIds") List<UUID> permissionIds);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{id}")
    boolean removePermissionsByRoleId(UUID id);

    boolean removePermissionsByRoleIds(@Param("roleIds") List<UUID> roleIds);

    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{id}")
    int removePermissionsById(UUID id);

    int removePermissionsByIds(List<UUID> ids);
}
