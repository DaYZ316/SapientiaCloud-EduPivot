package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.config.GitHubOAuth2Properties;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import com.dayz.sapientiacloud_edupivot.auth.service.IGitHubOAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * GitHub OAuth2服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubOAuth2ServiceImpl implements IGitHubOAuth2Service {

    private final SysUserClient sysUserClient;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final GitHubOAuth2Properties gitHubOAuth2Properties;

    @Override
    public String generateState() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String buildAuthorizeUrl(String state) {
        return String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                gitHubOAuth2Properties.getClientId(),
                gitHubOAuth2Properties.getRedirectUri(),
                gitHubOAuth2Properties.getScope(),
                state
        );
    }

    @Override
    public Result<Map<String, Object>> handleOAuthCallback(String code, String state) {
        try {
            // 获取accessToken
            String accessToken = getAccessToken(code);
            // 获取GitHub用户信息
            Map<String, Object> userInfo = getGitHubUserInfo(accessToken);
            // 查找或创建系统用户
            SysUserInternalVO user = findOrCreateUser(userInfo);
            // 生成token
            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            Map<String, Object> result = Map.of(
                    "accessToken", token,
                    "refreshToken", refreshToken,
                    "user", user
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("GitHub OAuth2回调处理失败: {}", e.getMessage(), e);
            return Result.fail("GitHub OAuth2回调处理失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", gitHubOAuth2Properties.getClientId());
        params.add("client_secret", gitHubOAuth2Properties.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", gitHubOAuth2Properties.getRedirectUri());
        
        log.debug("请求GitHub获取access_token，redirect_uri: {}", gitHubOAuth2Properties.getRedirectUri());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request, (Class<Map<String, Object>>)(Class<?>)Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("access_token")) {
                log.debug("成功获取access_token");
                return (String) responseBody.get("access_token");
            } else {
                // GitHub返回业务错误，不应该重试（因为code只能使用一次）
                String error = (String) responseBody.get("error");
                String errorDescription = (String) responseBody.get("error_description");
                log.error("GitHub返回错误 - error: {}, error_description: {}, 完整响应: {}", error, errorDescription, responseBody);
                
                // 对于bad_verification_code等错误，提供更友好的错误信息
                if ("bad_verification_code".equals(error)) {
                    throw new RuntimeException("授权码已过期或已被使用，请重新登录: " + errorDescription);
                }
                throw new RuntimeException("获取access_token失败: " + error + " - " + errorDescription);
            }
        } catch (org.springframework.web.client.RestClientException e) {
            // 网络异常，可以重试
            log.error("网络异常，获取access_token失败: {}", e.getMessage(), e);
            throw new RuntimeException("网络异常，获取access_token失败: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // 业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            log.error("获取access_token时发生未知异常: {}", e.getMessage(), e);
            throw new RuntimeException("获取access_token失败: " + e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getGitHubUserInfo(String accessToken) {
        String url = "https://api.github.com/user";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        HttpEntity<String> request = new HttpEntity<>(headers);
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.debug("尝试获取GitHub用户信息，第{}次", i + 1);
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, request, (Class<Map<String, Object>>)(Class<?>)Map.class);
                return response.getBody();
            } catch (Exception e) {
                log.warn("获取GitHub用户信息失败，第{}次尝试: {}", i + 1, e.getMessage());
                if (i == maxRetries - 1) {
                    throw new RuntimeException("获取GitHub用户信息失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        throw new RuntimeException("获取GitHub用户信息失败");
    }

    @Override
    public SysUserInternalVO findOrCreateUser(Map<String, Object> githubUserInfo) {
        Map<String, String> userInfo = com.dayz.sapientiacloud_edupivot.auth.common.security.utils.GitHubUserInfoUtil.extractUserInfo(githubUserInfo);
        String githubId = userInfo.get("githubId");
        String username = userInfo.get("username");
        String email = userInfo.get("email");
        String name = userInfo.get("name");
        String avatarUrl = userInfo.get("avatarUrl");
        Result<SysUserInternalVO> result = sysUserClient.findOrCreateByThirdParty("github", githubId, username, email, name, avatarUrl);
        if (result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            throw new RuntimeException("查找或创建用户失败: " + result.getMessage());
        }
    }
}

