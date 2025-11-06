package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.constant.OAuth2Constants;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.OAuth2CallbackResultDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.ThirdPartyLoginResultVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.config.OAuth2Config;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.GitHubUserInfoUtil;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import com.dayz.sapientiacloud_edupivot.auth.service.IGitHubOAuth2Service;
import com.dayz.sapientiacloud_edupivot.auth.utils.OAuth2RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * GitHub OAuth2服务实现类
 */
@Service
@RequiredArgsConstructor
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

        String url = UriComponentsBuilder.fromUriString(OAuth2Constants.GITHUB_AUTHORIZE_URL)
                .queryParam(OAuth2Constants.PARAM_CLIENT_ID, clientId)
                .queryParam(OAuth2Constants.PARAM_REDIRECT_URI, redirectUri)
                .queryParam(OAuth2Constants.PARAM_SCOPE, scope)
                .queryParam(OAuth2Constants.PARAM_STATE, state)
                .build()
                .toUriString();

        return url;
    }

    @Override
    public Result<OAuth2CallbackResultDTO> handleOAuthCallback(String code, String state) {
        try {
            String accessToken = getAccessToken(code);
            Map<String, Object> userInfo = getGitHubUserInfo(accessToken);
            ThirdPartyLoginResultVO loginResult = findOrCreateUserWithStatus(userInfo);
            String token = jwtUtil.generateToken(loginResult.getUser());
            String refreshToken = jwtUtil.generateRefreshToken(loginResult.getUser());
            loginResult.getUser().setPassword(null);

            OAuth2CallbackResultDTO.RegistrationStatusDTO registrationStatus =
                    OAuth2CallbackResultDTO.RegistrationStatusDTO.builder()
                            .isNewUser(loginResult.getIsNewUser())
                            .needBindMobile(loginResult.getNeedBindMobile())
                            .needSelectIdentity(loginResult.getNeedSelectIdentity())
                            .needCompleteInfo(loginResult.getNeedCompleteInfo())
                            .currentStep(loginResult.getCurrentStep())
                            .build();

            OAuth2CallbackResultDTO resultDTO = OAuth2CallbackResultDTO.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .user(loginResult.getUser())
                    .registrationStatus(registrationStatus)
                    .build();

            return Result.success(resultDTO);
        } catch (Exception e) {
            return Result.fail("GitHub OAuth2回调处理失败: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getAccessToken(String code) {
        String url = OAuth2Constants.GITHUB_ACCESS_TOKEN_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.set(OAuth2Constants.HEADER_ACCEPT, OAuth2Constants.ACCEPT_JSON);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(OAuth2Constants.PARAM_CLIENT_ID, OAuth2Config.getClientId());
        params.add(OAuth2Constants.PARAM_CLIENT_SECRET, OAuth2Config.getClientSecret());
        params.add(OAuth2Constants.PARAM_CODE, code);
        params.add(OAuth2Constants.PARAM_REDIRECT_URI, OAuth2Config.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        return OAuth2RetryUtil.executeWithRetry("获取access_token", () -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    url, request, (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey(OAuth2Constants.RESPONSE_ACCESS_TOKEN)) {
                return (String) responseBody.get(OAuth2Constants.RESPONSE_ACCESS_TOKEN);
            } else {
                String error = (String) responseBody.get(OAuth2Constants.RESPONSE_ERROR);

                if (OAuth2Constants.ERROR_BAD_VERIFICATION_CODE.equals(error)) {
                    throw new BusinessException(OAuth2Enum.AUTHORIZATION_CODE_EXPIRED);
                }
                throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getGitHubUserInfo(String accessToken) {
        String url = OAuth2Constants.GITHUB_USER_API_URL;
        HttpHeaders headers = new HttpHeaders();
        headers.set(OAuth2Constants.HEADER_AUTHORIZATION, OAuth2Constants.AUTHORIZATION_BEARER_PREFIX + accessToken);
        headers.set(OAuth2Constants.HEADER_ACCEPT, OAuth2Constants.ACCEPT_GITHUB_V3);
        HttpEntity<String> request = new HttpEntity<>(headers);

        Map<String, Object> userInfo = OAuth2RetryUtil.executeWithRetry("获取GitHub用户信息", () -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class);
            return response.getBody();
        });

        if (userInfo == null || userInfo.isEmpty()) {
            throw new BusinessException(OAuth2Enum.OAUTH2_USER_INFO_INVALID);
        }

        if (!userInfo.containsKey(OAuth2Constants.GITHUB_API_USER_ID)) {
            throw new BusinessException(OAuth2Enum.OAUTH2_USER_INFO_INVALID);
        }

        return userInfo;
    }

    private ThirdPartyLoginResultVO findOrCreateUserWithStatus(Map<String, Object> githubUserInfo) {
        Map<String, String> userInfo = GitHubUserInfoUtil.extractUserInfo(githubUserInfo);
        String githubId = userInfo.get(OAuth2Constants.USER_INFO_GITHUB_ID);
        String username = userInfo.get(OAuth2Constants.USER_INFO_USERNAME);
        String email = userInfo.get(OAuth2Constants.USER_INFO_EMAIL);
        String name = userInfo.get(OAuth2Constants.USER_INFO_NAME);
        String avatarUrl = userInfo.get(OAuth2Constants.USER_INFO_AVATAR_URL);
        try {
            Result<ThirdPartyLoginResultVO> result = sysUserClient.findOrCreateByThirdParty(OAuth2Constants.PROVIDER_GITHUB, githubId, username, email, name, avatarUrl);

            if (result.isSuccess() && result.getData() != null) {
                return result.getData();
            } else {
                throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
        }
    }
}



