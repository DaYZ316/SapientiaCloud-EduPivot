package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录相关接口")
public class AuthController {

    private final AuthService authService;
    private final SysUserClient sysUserClient;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码登录系统")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO sysUserLoginDTO) {
        log.info("用户登录请求: 用户名: {}", sysUserLoginDTO.getUsername());
        SysUserLoginVO loginVO = authService.login(sysUserLoginDTO);
        return Result.success(loginVO);
    }
    
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证JWT令牌是否有效")
    public Result<Boolean> validateToken(@RequestParam("token") String token) {
        boolean isValid = authService.validateToken(token);
        return Result.success(isValid);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "使当前JWT令牌失效")
    public Result<Boolean> logout(HttpServletRequest request) {
        boolean result = authService.logout(request);
        return Result.success(result);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册一个新的用户")
    public Result<Boolean> register(@Valid @RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return sysUserClient.registerUser(sysUserRegisterDTO);
    }

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    public Result<SysUserInternalVO> getUserInfo(HttpServletRequest request) {
        return authService.getUserInfo(request);
    }

    @PutMapping("/password")
    @Operation(summary = "更新密码", description = "更新当前登录用户的密码")
    public Result<Boolean> updatePassword(HttpServletRequest request, @Valid @RequestBody SysUserPasswordDTO sysUserPasswordDTO) {
        return Result.success(authService.updatePassword(request, sysUserPasswordDTO));
    }
}