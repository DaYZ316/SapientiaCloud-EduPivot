package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.provider.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * 统一的第三方登录控制器。
 * 路径规范为 /api/auth/oauth2/**，便于网关与安全白名单统一管理。
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final Map<String, OAuthProvider> oAuthProviderMap;

    /**
     * 发起授权。
     * 重定向到第三方平台的授权页。
     */
    @GetMapping("/oauth2/authorize/{provider}")
    public void authorize(@PathVariable("provider") String provider, HttpServletResponse response) throws Exception {
        OAuthProvider oAuthProvider = resolveProvider(provider);
        String state = UUID.randomUUID().toString();
        String authorizeUrl = oAuthProvider.buildAuthorizeUrl(state);
        response.sendRedirect(authorizeUrl);
    }

    /**
     * 授权回调。
     * 第三方平台会带回 code 与 state。
     */
    @GetMapping("/oauth2/callback/{provider}")
    public Result<Map<String, Object>> callback(@PathVariable("provider") String provider,
                                                @RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        OAuthProvider oAuthProvider = resolveProvider(provider);
        Map<String, Object> data = oAuthProvider.handleCallback(code, state);
        return Result.success(data);
    }

    private OAuthProvider resolveProvider(String provider) {
        log.info("尝试获取 provider: {}", provider);
        log.info("已注册的 provider: {}", oAuthProviderMap.keySet());
        OAuthProvider oAuthProvider = oAuthProviderMap.get(provider);
        if (oAuthProvider == null) {
            log.error("不支持的第三方登录提供商: {}，当前map内容: {}", provider, oAuthProviderMap.keySet());
            throw new IllegalArgumentException(String.format("不支持的第三方登录提供商: %s", provider));
        }
        return oAuthProvider;
    }
}


