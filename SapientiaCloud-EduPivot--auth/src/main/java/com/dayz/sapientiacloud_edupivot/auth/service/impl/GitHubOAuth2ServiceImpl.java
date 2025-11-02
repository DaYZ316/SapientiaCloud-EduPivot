package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.security.config.OAuth2Config;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.ThirdPartyLoginResultVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.GitHubUserInfoUtil;
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
    private final OAuth2Config OAuth2Config;

    @Override
    public String buildAuthorizeUrl(String state) {
        String clientId = OAuth2Config.getClientId();
        String redirectUri = OAuth2Config.getRedirectUri();
        String scope = OAuth2Config.getScope();
        
        // 配置已在 OAuth2Config.validate() 中验证，此处直接使用
        String url = String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                clientId, redirectUri, scope, state
        );
        log.debug("构建GitHub授权URL: {}", url.replace(clientId, "***"));
        return url;
    }

    @Override
    public Result<Map<String, Object>> handleOAuthCallback(String code, String state) {
        try {
            // 获取accessToken
            String accessToken = getAccessToken(code);
            // 获取GitHub用户信息
            Map<String, Object> userInfo = getGitHubUserInfo(accessToken);
            // 查找或创建系统用户（返回注册状态）
            ThirdPartyLoginResultVO loginResult = findOrCreateUserWithStatus(userInfo);
            // 生成token（即使未完成注册也生成，用于后续接口认证）
            String token = jwtUtil.generateToken(loginResult.getUser());
            String refreshToken = jwtUtil.generateRefreshToken(loginResult.getUser());
            
            Map<String, Object> result = Map.of(
                    "accessToken", token,
                    "refreshToken", refreshToken,
                    "user", loginResult.getUser(),
                    "registrationStatus", Map.of(
                            "isNewUser", loginResult.getIsNewUser(),
                            "needBindMobile", loginResult.getNeedBindMobile(),
                            "needSelectIdentity", loginResult.getNeedSelectIdentity(),
                            "needCompleteInfo", loginResult.getNeedCompleteInfo(),
                            "currentStep", loginResult.getCurrentStep()
                    )
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
        params.add("client_id", OAuth2Config.getClientId());
        params.add("client_secret", OAuth2Config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", OAuth2Config.getRedirectUri());
        
        log.debug("请求GitHub获取access_token，redirect_uri: {}", OAuth2Config.getRedirectUri());
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        // 添加重试机制（仅针对网络错误）
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.debug("尝试获取access_token，第{}次", i + 1);
                ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request, (Class<Map<String, Object>>)(Class<?>)Map.class);
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody != null && responseBody.containsKey("access_token")) {
                    log.debug("成功获取access_token");
                    return (String) responseBody.get("access_token");
                } else {
                    // GitHub返回业务错误，不应该重试（因为code可能已被消费）
                    String error = (String) responseBody.get("error");
                    String errorDescription = (String) responseBody.get("error_description");
                    log.error("GitHub返回错误 - error: {}, error_description: {}, 完整响应: {}", error, errorDescription, responseBody);
                    
                    if ("bad_verification_code".equals(error)) {
                        throw new BusinessException(OAuth2Enum.AUTHORIZATION_CODE_EXPIRED);
                    }
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
            } catch (org.springframework.web.client.ResourceAccessException e) {
                // 网络异常（连接超时、读取超时等），可以重试
                log.warn("网络异常，获取access_token失败，第{}次尝试: {}", i + 1, e.getMessage());
                if (i == maxRetries - 1) {
                    log.error("网络异常，获取access_token失败，已重试{}次: {}", maxRetries, e.getMessage(), e);
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
                try {
                    Thread.sleep(1000 * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // HTTP客户端错误（4xx），通常是业务错误，不重试
                log.error("GitHub API返回客户端错误: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
                if (e.getStatusCode().value() == 400) {
                    // 可能是bad_verification_code等
                    throw new BusinessException(OAuth2Enum.AUTHORIZATION_CODE_EXPIRED);
                }
                throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
            } catch (BusinessException e) {
                // 业务异常，直接抛出（不重试）
                throw e;
            } catch (Exception e) {
                log.error("获取access_token时发生未知异常，第{}次尝试: {}", i + 1, e.getMessage(), e);
                if (i == maxRetries - 1) {
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
                try {
                    Thread.sleep(1000 * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
            }
        }
        throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
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
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
                }
            }
        }
        throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
    }

    /**
     * 查找或创建用户并返回注册状态
     */
    private ThirdPartyLoginResultVO findOrCreateUserWithStatus(Map<String, Object> githubUserInfo) {
        Map<String, String> userInfo = GitHubUserInfoUtil.extractUserInfo(githubUserInfo);
        String githubId = userInfo.get("githubId");
        String username = userInfo.get("username");
        String email = userInfo.get("email");
        String name = userInfo.get("name");
        String avatarUrl = userInfo.get("avatarUrl");
        try {
            Result<ThirdPartyLoginResultVO> result = sysUserClient.findOrCreateByThirdParty("github", githubId, username, email, name, avatarUrl);
            log.info("Feign 调用结果: success={}, message={}", result.isSuccess(), result.getMessage());
            
            if (result.isSuccess() && result.getData() != null) {
                ThirdPartyLoginResultVO loginResult = result.getData();
                log.info("用户查找/创建成功: userId={}, username={}, isNewUser={}, currentStep={}", 
                        loginResult.getUser().getId(), loginResult.getUser().getUsername(), 
                        loginResult.getIsNewUser(), loginResult.getCurrentStep());
                return loginResult;
            } else {
                log.error("查找或创建用户失败: success={}, message={}, code={}", 
                        result.isSuccess(), result.getMessage(), result.getCode());
                throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Feign 调用异常: {}", e.getMessage(), e);
            throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
        }
    }
}



