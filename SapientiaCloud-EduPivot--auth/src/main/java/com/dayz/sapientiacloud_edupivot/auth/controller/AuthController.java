package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LogoutDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.LogoutVO;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "用户名密码登录获取token")
    @PostMapping("/login")
    public Result<String> login(
            @Parameter(description = "登录请求对象", required = true) @RequestBody LoginDTO loginDTO
    ) {
        
        // 调用认证服务进行登录验证，返回JWT令牌
        String token = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success(token);
    }

    @Operation(summary = "验证Token", description = "验证token是否有效")
    @PostMapping("/validate")
    public Result<Boolean> validateToken(
            @Parameter(description = "JWT Token", required = true) @RequestParam("token") String token
    ) {
        boolean valid = authService.validateToken(token);
        return Result.success(valid);
    }

    @Operation(summary = "用户登出", description = "自动获取token进行登出，支持单设备或全设备登出")
    @PostMapping("/logout/enhanced")
    public Result<LogoutVO> logoutEnhanced(
            HttpServletRequest request,
            @Parameter(description = "登出请求参数", required = false) @RequestBody(required = false) LogoutDTO logoutDTO
    ) {
        LogoutVO response = authService.logoutEnhanced(request, logoutDTO);

        return Result.success(response);
    }

    @Operation(summary = "用户登出（带Token）", description = "使用指定token进行登出")
    @PostMapping("/logout/with-token")
    public Result<LogoutVO> logoutWithToken(
            @Parameter(description = "JWT Token", required = true) @RequestParam("token") String token,
            @Parameter(description = "登出请求参数", required = false) @RequestBody(required = false) LogoutDTO logoutDTO,
            HttpServletRequest request
    ) {
        
        // 获取客户端真实IP地址，用于安全审计和日志记录
        String clientIp = getClientIpAddress(request);
        
        // 调用认证服务执行带令牌的登出操作
        LogoutVO response = authService.logoutWithToken(token, logoutDTO, clientIp);
        
        // 返回登出结果
        return Result.success(response);
    }

    /**
     * 管理员强制登出用户所有设备接口
     *
     * @param username 目标用户名，要强制登出的用户账户
     * @param reason 强制登出的原因，用于审计日志记录（可选，默认为"管理员强制登出"）
     * @return 登出结果，返回被撤销的令牌数量
     */
    @Operation(summary = "强制登出所有设备", description = "管理员强制用户登出所有设备")
    @PostMapping("/logout/force/{username}")
    public Result<Integer> forceLogoutAllDevices(
            @Parameter(description = "用户名", required = true) @PathVariable("username") String username,
            @Parameter(description = "登出原因", required = false) @RequestParam(value = "reason", defaultValue = "管理员强制登出") String reason
    ) {
        
        // 调用认证服务执行强制登出操作，返回被撤销的令牌数量
        int invalidatedCount = authService.forceLogoutAllDevices(username, reason);
        
        // 返回操作结果
        return Result.success(invalidatedCount);
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象，包含请求头信息
     * @return 客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // 优先从X-Forwarded-For头获取IP（处理代理情况）
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For格式：client, proxy1, proxy2
            // 取第一个IP作为客户端真实IP
            return xForwardedFor.split(",")[0].trim();
        }

        // 尝试从X-Real-IP头获取（某些代理服务器使用）
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 最后使用直连IP（没有代理的情况）
        return request.getRemoteAddr();
    }
} 