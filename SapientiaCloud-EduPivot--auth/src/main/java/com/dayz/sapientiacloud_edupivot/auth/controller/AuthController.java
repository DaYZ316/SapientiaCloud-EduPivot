package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserMobileLoginDTO;
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

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录相关接口")
public class AuthController {

    private final AuthService authService;
    private final SysUserClient sysUserClient;

    @PostMapping("/login")
    @Operation(summary = "login", description = "通过用户名和密码登录系统")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO sysUserLoginDTO) {
        SysUserLoginVO loginVO = authService.login(sysUserLoginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/mobile-login")
    @Operation(summary = "mobileLogin", description = "通过手机号和验证码登录系统")
    public Result<SysUserLoginVO> mobileLogin(@Valid @RequestBody SysUserMobileLoginDTO sysUserMobileLoginDTO) {
        SysUserLoginVO loginVO = authService.mobileLogin(sysUserMobileLoginDTO);
        return Result.success(loginVO);
    }

    @GetMapping("/validate")
    @Operation(summary = "validateToken", description = "验证JWT令牌是否有效")
    public Result<Boolean> validateToken(@RequestParam("token") String token) {
        boolean isValid = authService.validateToken(token);
        return Result.success(isValid);
    }

    @PostMapping("/logout")
    @Operation(summary = "logout", description = "使当前JWT令牌失效")
    public Result<Boolean> logout(HttpServletRequest request) {
        boolean result = authService.logout(request);
        return Result.success(result);
    }

    @PostMapping("/register")
    @Operation(summary = "register", description = "注册一个新的用户")
    public Result<Boolean> register(@Valid @RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return sysUserClient.registerUser(sysUserRegisterDTO);
    }

    @GetMapping("/info")
    @Operation(summary = "getUserInfo", description = "获取当前登录用户的信息")
    public Result<SysUserInternalVO> getUserInfo(HttpServletRequest request) {
        return authService.getUserInfo(request);
    }

    @PutMapping("/password")
    @Operation(summary = "updatePassword", description = "更新当前登录用户的密码")
    public Result<Boolean> updatePassword(HttpServletRequest request, @Valid @RequestBody SysUserPasswordDTO sysUserPasswordDTO) {
        return Result.success(authService.updatePassword(request, sysUserPasswordDTO));
    }
}