package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RefreshTokenRequest;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

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
    @Parameter(name = "Authorization", hidden = true)
    public Result<String> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.substring(7);
            jwtUtil.logout(token);
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage());
            return Result.fail("登出失败: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证当前令牌是否有效")
    @Parameter(name = "Authorization", hidden = true)
    public Result<Boolean> validateToken(
            @Parameter(description = "JWT Token", required = true) @RequestParam("token") String token
    ) {
        // 调用认证服务验证令牌有效性
        boolean valid = jwtUtil.validateToken(token);

        // 返回验证结果
        return Result.success(valid);
    }

    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "根据令牌获取当前用户信息")
    @Parameter(name = "Authorization", hidden = true)
    public Result<Object> getUserInfo(Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // 在实际场景中，您可能需要一个方法来通过用户名从SysUser中查找完整的UUID
            // 这里我们假设UserDetails的username就是SysUser的username，并以此调用服务
            // 注意：UserDetailsServiceImpl需要确保返回的UserDetails的username是准确的
            // 为了演示，此处直接返回 UserDetails，实际应返回业务VO
            return Result.success(userDetails);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return Result.fail("获取用户信息失败: " + e.getMessage());
        }
    }
}