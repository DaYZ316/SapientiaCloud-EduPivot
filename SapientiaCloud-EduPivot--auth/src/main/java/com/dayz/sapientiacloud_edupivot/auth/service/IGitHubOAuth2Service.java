package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.vo.OAuth2CallbackResultDTO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;

import java.util.Map;

/**
 * GitHub OAuth2服务接口
 *
 * @author 34571
 */
public interface IGitHubOAuth2Service {

    String buildAuthorizeUrl(String state);

    Result<OAuth2CallbackResultDTO> handleOAuthCallback(String code, String state);

    String getAccessToken(String code);

    Map<String, Object> getGitHubUserInfo(String accessToken);
}

