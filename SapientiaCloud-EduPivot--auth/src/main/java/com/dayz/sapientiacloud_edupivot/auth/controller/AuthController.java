package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RefreshTokenRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RegisterRequest;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int TOKEN_LENGTH = 7;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authService.register(registerRequest);
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            LoginResponse loginResponse = authService.refreshToken(refreshTokenRequest);
            return Result.success(loginResponse);
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            return Result.fail("刷新令牌失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出并清除令牌")
    public Result<String> logout(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token != null) {
                jwtUtil.logout(token);
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return Result.fail("登出失败: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证当前令牌是否有效")
    public Result<Boolean> validateToken(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return Result.fail("缺少令牌");
            }
            
            boolean isValid = jwtUtil.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return Result.fail("令牌验证失败: " + e.getMessage());
        }
    }

    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "根据令牌获取当前用户信息")
    public Result<Object> getUserInfo(HttpServletRequest request) {
        try {
            String token = getTokenFromRequest(request);
            if (token == null) {
                return Result.fail("缺少令牌");
            }
            
            if (!jwtUtil.validateToken(token)) {
                return Result.fail("令牌无效");
            }
            
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            
            // 这里可以根据需要返回更多用户信息
            Object userInfo = authService.getUserInfo(userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.fail("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_LENGTH);
        }
        return null;
    }
}