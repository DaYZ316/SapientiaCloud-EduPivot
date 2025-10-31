package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.provider.OAuthProvider;
import com.github.f4b6a3.uuid.UuidCreator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2.0接口", description = "第三方登录接口")
public class OAuthController {

    private final Map<String, OAuthProvider> oAuthProviderMap;

    @GetMapping("/authorize/{provider}")
    @Operation(summary = "authorize", description = "发起授权")
    public void authorize(
            @Parameter(name = "provider", description = "第三方登录提供商名称") @PathVariable("provider") String provider,
            @Parameter(name = "response", description = "响应对象") HttpServletResponse response
    ) throws Exception {
        OAuthProvider oAuthProvider = resolveProvider(provider);
        String state = UuidCreator.getTimeOrderedEpoch().toString();
        String authorizeUrl = oAuthProvider.buildAuthorizeUrl(state);
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback/{provider}")
    @Operation(summary = "callback", description = "处理授权回调")
    public Result<Map<String, Object>> callback(
            @Parameter(name = "provider", description = "第三方登录提供商名称") @PathVariable("provider") String provider,
            @Parameter(name = "code", description = "授权码") @RequestParam("code") String code,
            @Parameter(name = "state", description = "状态参数") @RequestParam("state") String state) {
        OAuthProvider oAuthProvider = resolveProvider(provider);
        Map<String, Object> data = oAuthProvider.handleCallback(code, state);
        return Result.success(data);
    }

    private OAuthProvider resolveProvider(String provider) {
        OAuthProvider oAuthProvider = oAuthProviderMap.get(provider);
        if (oAuthProvider == null) {
            log.error("不支持的第三方登录提供商: {}，当前map内容: {}", provider, oAuthProviderMap.keySet());
            throw new IllegalArgumentException(String.format("不支持的第三方登录提供商: %s", provider));
        }
        return oAuthProvider;
    }
}


