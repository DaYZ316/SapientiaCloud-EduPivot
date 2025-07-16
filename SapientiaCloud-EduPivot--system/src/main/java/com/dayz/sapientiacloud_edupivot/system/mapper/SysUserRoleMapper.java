package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysUserRoleMapper {

    List<SysRoleVO> getUserRoles(UUID userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<UUID> getUserRoleIds(UUID userId);

    int addUserRoles(@Param("userId") UUID userId, @Param("roleIds") List<UUID> roleIds);

    int removeUserRoles(@Param("userId") UUID userId, @Param("roleIds") List<UUID> roleIds);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{id}")
    boolean removeRolesByUserId(UUID id);

    boolean removeRolesByUserIds(@Param("userIds") List<UUID> userIds);

    List<SysRole> getRolesByUserIds(@Param("userIds") List<UUID> userIds);
}
