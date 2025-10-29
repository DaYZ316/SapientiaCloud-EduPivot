package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class GitHubOAuth2Controller {

    private final SysUserClient sysUserClient;
    private final JwtUtil jwtUtil;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;

    @GetMapping("/oauth2/authorize/github")
    public void githubAuthorize(jakarta.servlet.http.HttpServletResponse response) throws Exception {
        log.debug("开始GitHub OAuth2授权流程");
        
        // 生成state参数用于防止CSRF攻击
        String state = UUID.randomUUID().toString();
        
        // 构建GitHub授权URL
        String authUrl = String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=user:email,read:user&state=%s",
                githubClientId,
                "http://localhost:31600/login/oauth2/code/github",
                state
        );
        
        log.debug("重定向到GitHub授权页面: {}", authUrl);
        response.sendRedirect(authUrl);
    }

    @GetMapping("/login/oauth2/code/github")
    public Result<Map<String, Object>> githubCallback(@RequestParam("code") String code, 
                                                      @RequestParam("state") String state) {
        log.debug("收到GitHub OAuth2回调: code={}, state={}", code, state);
        
        try {

            String accessToken = getAccessToken(code);
            log.debug("获取到GitHub access_token: {}", accessToken);
            Map<String, Object> userInfo = getGitHubUserInfo(accessToken);
            log.debug("获取到GitHub用户信息: {}", userInfo);
            SysUserInternalVO user = findOrCreateUser(userInfo);
            log.debug("用户处理完成: {}", user.getUsername());
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

    private String getAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", githubClientId);
        params.add("client_secret", githubClientSecret);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.debug("尝试获取access_token，第{}次", i + 1);
                ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("access_token")) {
                    return (String) responseBody.get("access_token");
                } else {
                    log.error("GitHub返回错误: {}", responseBody);
                    throw new RuntimeException("获取access_token失败: " + responseBody);
                }
            } catch (Exception e) {
                log.warn("获取access_token失败，第{}次尝试: {}", i + 1, e.getMessage());
                if (i == maxRetries - 1) {
                    throw new RuntimeException("获取access_token失败，已重试" + maxRetries + "次: " + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        throw new RuntimeException("获取access_token失败");
    }

    private Map<String, Object> getGitHubUserInfo(String accessToken) {
        String url = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                log.debug("尝试获取GitHub用户信息，第{}次", i + 1);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
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

    private SysUserInternalVO findOrCreateUser(Map<String, Object> githubUserInfo) {
        String githubId = String.valueOf(githubUserInfo.get("id"));
        String username = (String) githubUserInfo.get("login");
        String email = (String) githubUserInfo.get("email");
        String name = (String) githubUserInfo.get("name");
        String avatarUrl = (String) githubUserInfo.get("avatar_url");
        
        // 调用系统服务查找或创建用户
        Result<SysUserInternalVO> result = sysUserClient.findOrCreateByThirdParty("github", githubId, username, email, name, avatarUrl);
        
        if (result.isSuccess() && result.getData() != null) {
            return result.getData();
        } else {
            throw new RuntimeException("查找或创建用户失败: " + result.getMessage());
        }
    }
}
