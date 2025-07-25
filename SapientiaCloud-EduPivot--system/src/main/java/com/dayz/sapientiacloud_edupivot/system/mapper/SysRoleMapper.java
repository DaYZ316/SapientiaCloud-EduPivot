package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysRoleQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<SysRoleVO> listSysRole(SysRoleQueryDTO sysRoleQueryDTO);

    @Select("SELECT * FROM sys_role WHERE id IN (#{roleIds})")
    List<SysRoleVO> getRolesByIds(List<UUID> roleIds);
}
