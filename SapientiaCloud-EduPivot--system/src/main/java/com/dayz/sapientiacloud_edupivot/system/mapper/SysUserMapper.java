package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysUserVO> listSysUser(SysUserQueryDTO sysUserQueryDTO);

    SysUser selectByUsername(String username);

    SysUser selectByGithubId(String githubId);

    SysUser selectByWechatId(String wechatId);

    SysUser selectByThirdPartyId(@Param("provider") String provider, @Param("providerId") String providerId);

    int physicalDeleteById(UUID id);
}