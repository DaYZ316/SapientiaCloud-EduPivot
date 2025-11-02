package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SelectIdentityDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;

/**
 * 身份选择服务接口
 */
public interface IdentityService {

    SysUserLoginVO selectIdentity(SelectIdentityDTO selectIdentityDTO);
}

