package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;

import java.util.Map;

/**
 * GitHub OAuth2服务接口
 * @author 34571
 */
public interface IGitHubOAuth2Service {

    String generateState();

    String buildAuthorizeUrl(String state);

    Result<Map<String, Object>> handleOAuthCallback(String code, String state);

    String getAccessToken(String code);

    Map<String, Object> getGitHubUserInfo(String accessToken);

    SysUserInternalVO findOrCreateUser(Map<String, Object> githubUserInfo);
}

