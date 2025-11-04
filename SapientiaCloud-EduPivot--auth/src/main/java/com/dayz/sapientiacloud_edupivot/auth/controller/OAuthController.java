package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.vo.OAuth2CallbackResultDTO;
import com.dayz.sapientiacloud_edupivot.auth.enums.OAuth2Enum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.provider.OAuthProvider;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.OAuth2StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2.0接口", description = "第三方登录接口")
public class OAuthController {

    private final Map<String, OAuthProvider> oAuthProviderMap;
    private final OAuth2StateService oAuth2StateService;

    @GetMapping("/authorize/{provider}")
    @Operation(summary = "authorize", description = "发起授权")
    public void authorize(
            @Parameter(name = "provider", description = "第三方登录提供商名称") @PathVariable("provider") String provider,
            @Parameter(name = "response", description = "响应对象") HttpServletResponse response
    ) throws Exception {
        // 验证provider是否支持并获取OAuthProvider实例
        OAuthProvider oAuthProvider = resolveProvider(provider);

        // 生成并存储state（防止CSRF攻击）
        String state = oAuth2StateService.generateAndStoreState(provider);

        // 构建授权URL并重定向
        String authorizeUrl = oAuthProvider.buildAuthorizeUrl(state);

        log.debug("OAuth2授权请求: provider={}, state={}", provider, state);
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback/{provider}")
    @Operation(summary = "callback", description = "处理授权回调")
    public Result<OAuth2CallbackResultDTO> callback(
            @Parameter(name = "provider", description = "第三方登录提供商名称") @PathVariable("provider") String provider,
            @Parameter(name = "code", description = "授权码") @RequestParam("code") String code,
            @Parameter(name = "state", description = "状态参数") @RequestParam("state") String state) {

        if (!StringUtils.hasText(code)) {
            throw new BusinessException(OAuth2Enum.OAUTH2_CALLBACK_FAILED);
        }
        if (!StringUtils.hasText(state)) {
            throw new BusinessException(OAuth2Enum.STATE_INVALID);
        }

        OAuthProvider oAuthProvider = resolveProvider(provider);

        oAuth2StateService.validateState(state, provider);

        OAuth2CallbackResultDTO data = oAuthProvider.handleCallback(code, state);

        log.debug("OAuth2回调处理成功: provider={}", provider);
        return Result.success(data);
    }

    private OAuthProvider resolveProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException(OAuth2Enum.PROVIDER_NOT_SUPPORTED);
        }

        OAuthProvider oAuthProvider = oAuthProviderMap.get(provider);
        if (oAuthProvider == null) {
            log.error("不支持的第三方登录提供商: {}，当前map内容: {}", provider, oAuthProviderMap.keySet());
            throw new BusinessException(OAuth2Enum.PROVIDER_NOT_SUPPORTED);
        }
        return oAuthProvider;
    }
}


