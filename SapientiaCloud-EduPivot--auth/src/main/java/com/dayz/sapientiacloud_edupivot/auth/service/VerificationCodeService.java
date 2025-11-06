package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SendVerificationCodeDTO;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {

    boolean sendVerificationCode(SendVerificationCodeDTO sendVerificationCodeDTO);

    boolean verifyCode(String mobile, String code);
}
