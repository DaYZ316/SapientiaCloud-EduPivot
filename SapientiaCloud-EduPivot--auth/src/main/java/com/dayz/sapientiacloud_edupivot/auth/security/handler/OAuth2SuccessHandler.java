package com.dayz.sapientiacloud_edupivot.auth.security.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.config.JwtConfig;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2认证成功处理器
 * 处理GitHub等第三方登录成功后的逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtConfig jwtConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SysUserClient sysUserClient;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
                // 获取OAuth2用户信息
                DefaultOAuth2User oauth2User = (DefaultOAuth2User) authentication.getPrincipal();
                String registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                log.info("第三方登录成功: provider={}, principal={}", registrationId, oauth2User.getAttributes());

                // 根据不同的OAuth2提供商处理用户信息
                if ("github".equals(registrationId)) {
                    handleGithubLogin(request, response, oauth2User, oauth2Token);
                } else {
                    // 其他OAuth2提供商的处理逻辑
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJsonResponse(response, Result.fail("不支持的第三方登录方式"));
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeJsonResponse(response, Result.fail("认证信息类型错误"));
            }
        } catch (Exception e) {
            log.error("OAuth2登录处理失败: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJsonResponse(response, Result.fail("登录处理失败，请稍后重试"));
        }
    }

    /**
     * 处理GitHub登录
     */
    private void handleGithubLogin(HttpServletRequest request, HttpServletResponse response, DefaultOAuth2User oauth2User, OAuth2AuthenticationToken oauth2Token) throws IOException {
        try {
            // 从GitHub获取用户信息
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, String> userInfo = com.dayz.sapientiacloud_edupivot.auth.common.security.utils.GitHubUserInfoUtil.extractUserInfo(attributes);
            
            String githubId = userInfo.get("githubId");
            String username = userInfo.get("username");
            String email = userInfo.get("email");
            String name = userInfo.get("name");
            String avatarUrl = userInfo.get("avatarUrl");
            
            // 查找或创建用户
            SysUserInternalVO user = findOrCreateUser(githubId, username, email, name, avatarUrl);
            if (user == null) {
                throw new BusinessException("用户信息处理失败");
            }
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);
            
            // 保存用户会话信息
            saveUserSession(user, token, refreshToken);
            
            // 构建响应数据
            Map<String, Object> result = Map.of(
                    "accessToken", token,
                    "refreshToken", refreshToken,
                    "user", user
            );
            
            // 写入JSON响应
            writeJsonResponse(response, Result.success(result));
        } catch (BusinessException e) {
            log.error("GitHub用户处理失败: {}", e.getMessage());
            writeJsonResponse(response, Result.fail(e.getMessage()));
        } catch (Exception e) {
            log.error("GitHub登录处理异常: {}", e.getMessage(), e);
            writeJsonResponse(response, Result.fail("GitHub登录处理失败"));
        }
    }

    /**
     * 查找或创建用户
     */
    private SysUserInternalVO findOrCreateUser(String githubId, String username, String email, String name, String avatarUrl) {
        try {
            // 调用系统服务查找或创建用户
            Result<SysUserInternalVO> result = sysUserClient.findOrCreateByThirdParty("github", githubId, username, email, name, avatarUrl);
            if (result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
            log.error("查找或创建用户失败: {}", result.getMessage());
            return null;
        } catch (Exception e) {
            log.error("调用系统服务失败: {}", e.getMessage(), e);
            throw new BusinessException("用户信息处理失败");
        }
    }

    /**
     * 保存用户会话信息
     */
    private void saveUserSession(SysUserInternalVO user, String token, String refreshToken) {
        // 保存用户信息到Redis，用于快速获取
        String userKey = "user:" + user.getId();
        redisTemplate.opsForValue().set(userKey, user, Duration.ofMillis(jwtConfig.getExpiration()));
        
        // 保存token黑名单，用于登出时失效
        String tokenKey = "token:" + token;
        redisTemplate.opsForValue().set(tokenKey, user.getId(), Duration.ofMillis(jwtConfig.getExpiration()));
    }

    /**
     * 写入JSON响应
     */
    private void writeJsonResponse(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(JSON.toJSONString(result));
            out.flush();
        }
    }
}