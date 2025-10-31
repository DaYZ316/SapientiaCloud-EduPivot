package com.dayz.sapientiacloud_edupivot.auth.service.provider;

import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.IGitHubOAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GitHub 登录提供方实现。
 */
@Component("github")
@RequiredArgsConstructor
public class GithubOAuthProvider implements OAuthProvider {

    private final IGitHubOAuth2Service gitHubOAuth2Service;

    @Override
    public String getProviderId() {
        return "github";
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return gitHubOAuth2Service.buildAuthorizeUrl(state);
    }

    @Override
    public Map<String, Object> handleCallback(String code, String state) {
        Result<Map<String, Object>> result = gitHubOAuth2Service.handleOAuthCallback(code, state);
        if (result != null && result.isSuccess()) {
            return result.getData();
        }
        throw new IllegalStateException(result == null ? "GitHub 登录失败" : result.getMessage());
    }
}


