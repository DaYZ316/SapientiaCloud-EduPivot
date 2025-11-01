package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SelectIdentityDTO;

/**
 * 身份选择服务接口
 */
public interface IdentityService {

    /**
     * 选择身份并创建对应的学生或教师记录
     *
     * @param selectIdentityDTO 身份选择数据传输对象
     * @return 操作是否成功
     */
    Boolean selectIdentity(SelectIdentityDTO selectIdentityDTO);
}

