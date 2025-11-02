package com.dayz.sapientiacloud_edupivot.auth.service;

/**
 * OAuth2 State管理服务
 * 用于防止CSRF攻击，存储和验证state参数
 */
public interface OAuth2StateService {

    String generateAndStoreState(String provider);

    void validateState(String state, String provider);
}

