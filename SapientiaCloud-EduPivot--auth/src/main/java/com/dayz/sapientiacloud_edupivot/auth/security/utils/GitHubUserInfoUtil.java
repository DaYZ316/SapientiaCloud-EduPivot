package com.dayz.sapientiacloud_edupivot.auth.security.utils;

import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;

import java.util.Map;

/**
 * GitHub用户信息提取工具类
 */
public class GitHubUserInfoUtil {

    /**
     * 从GitHub用户属性中提取用户信息
     *
     * @param attributes GitHub用户属性
     * @return GitHub用户信息Map，包含githubId, username, email, name, avatarUrl
     */
    public static Map<String, String> extractUserInfo(Map<String, Object> attributes) {
        String githubId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("avatar_url");

        // 确保username不为空
        if (username == null || username.isEmpty()) {
            throw new BusinessException("GitHub用户名获取失败");
        }

        return Map.of(
                "githubId", githubId != null ? githubId : "",
                "username", username,
                "email", email != null ? email : "",
                "name", name != null ? name : "",
                "avatarUrl", avatarUrl != null ? avatarUrl : ""
        );
    }

    private GitHubUserInfoUtil() {
        // 禁止实例化
    }
}
