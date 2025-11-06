package com.dayz.sapientiacloud_edupivot.auth.security.utils;

import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;

import java.util.Map;


public class GitHubUserInfoUtil {

    private GitHubUserInfoUtil() {
        // 禁止实例化
    }

    /**
     * 从GitHub用户属性中提取用户信息
     *
     * @param attributes GitHub用户属性
     * @return GitHub用户信息Map，包含githubId, username, email, name, avatarUrl
     */
    public static Map<String, String> extractUserInfo(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            throw new BusinessException(OAuth2Enum.OAUTH2_USER_INFO_INVALID);
        }

        String githubId = String.valueOf(attributes.get(OAuth2Constants.GITHUB_API_USER_ID));
        String username = (String) attributes.get(OAuth2Constants.GITHUB_API_USER_LOGIN);
        String email = (String) attributes.get(OAuth2Constants.GITHUB_API_USER_EMAIL);
        String name = (String) attributes.get(OAuth2Constants.GITHUB_API_USER_NAME);
        String avatarUrl = (String) attributes.get(OAuth2Constants.GITHUB_API_USER_AVATAR_URL);

        if (username == null || username.isEmpty()) {
            throw new BusinessException(OAuth2Enum.OAUTH2_USER_INFO_INVALID);
        }

        return Map.of(
                OAuth2Constants.USER_INFO_GITHUB_ID, githubId != null ? githubId : "",
                OAuth2Constants.USER_INFO_USERNAME, username,
                OAuth2Constants.USER_INFO_EMAIL, email != null ? email : "",
                OAuth2Constants.USER_INFO_NAME, name != null ? name : "",
                OAuth2Constants.USER_INFO_AVATAR_URL, avatarUrl != null ? avatarUrl : ""
        );
    }
}
