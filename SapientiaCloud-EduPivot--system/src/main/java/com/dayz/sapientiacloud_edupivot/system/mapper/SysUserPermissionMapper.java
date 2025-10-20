package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysUserPermissionMapper {

    List<SysPermissionVO> getUserPermissions(@Param("userId") UUID userId);
}
