package com.dayz.sapientiacloud_edupivot.auth.provider.impl;

import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.OAuth2CallbackResultDTO;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.provider.OAuthProvider;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.IGitHubOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * GitHub 登录提供方实现。
 */
@Component(OAuth2Constants.PROVIDER_GITHUB)
@RequiredArgsConstructor
public class GithubOAuthProvider implements OAuthProvider {

    private final IGitHubOAuth2Service gitHubOAuth2Service;

    @Override
    public String getProviderId() {
        return OAuth2Constants.PROVIDER_GITHUB;
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return gitHubOAuth2Service.buildAuthorizeUrl(state);
    }

    @Override
    public OAuth2CallbackResultDTO handleCallback(String code, String state) {
        Result<OAuth2CallbackResultDTO> result = gitHubOAuth2Service.handleOAuthCallback(code, state);
        if (result != null && result.isSuccess() && result.getData() != null) {
            return result.getData();
        }
        if (result != null && result.getCode() != 0) {
            throw new BusinessException(result.getCode(), result.getMessage());
        }
        throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
    }
}


