package com.dayz.sapientiacloud_edupivot.auth.provider;

import java.util.Map;

/**
 * 第三方登录提供方统一接口。
 * 便于扩展 GitHub、WeChat 等不同平台的授权与回调处理。
 */
public interface OAuthProvider {

    String getProviderId();

    String buildAuthorizeUrl(String state);

    Map<String, Object> handleCallback(String code, String state);
}


