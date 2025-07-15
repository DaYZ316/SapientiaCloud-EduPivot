package com.dayz.sapientiacloud_edupivot.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysUserRoleMapper {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<UUID> getUserRoleIds(UUID userId);

    int addUserRoles(@Param("userId") UUID userId, @Param("roleIds") List<UUID> roleIds);

    int removeUserRoles(@Param("userId") UUID userId, @Param("roleIds") List<UUID> roleIds);

}
