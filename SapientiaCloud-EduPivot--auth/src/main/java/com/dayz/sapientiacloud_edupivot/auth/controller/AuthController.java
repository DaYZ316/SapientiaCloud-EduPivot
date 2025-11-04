package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.service.VerificationCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户登录相关接口")
public class AuthController {

    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

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
        Boolean result = authService.register(sysUserRegisterDTO);
        return Result.success(result);
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

    @PutMapping("/mobile-password")
    @Operation(summary = "updatePasswordByMobile", description = "通过手机验证码修改密码")
    public Result<Boolean> updatePasswordByMobile(@Valid @RequestBody SysUserMobilePasswordDTO sysUserMobilePasswordDTO) {
        return Result.success(authService.updatePasswordByMobile(sysUserMobilePasswordDTO));
    }

    @PostMapping("/send-code")
    @Operation(summary = "sendVerificationCode", description = "发送手机验证码")
    public Result<Boolean> sendVerificationCode(@Valid @RequestBody SendVerificationCodeDTO sendVerificationCodeDTO) {
        boolean result = verificationCodeService.sendVerificationCode(sendVerificationCodeDTO);
        return Result.success(result);
    }

    @GetMapping("/check-username")
    @Operation(summary = "checkUsername", description = "检查用户名是否可用")
    public Result<Boolean> checkUsername(@RequestParam("username") String username) {
        Boolean available = authService.checkUsernameAvailable(username);
        return Result.success(available);
    }

    @GetMapping("/check-mobile")
    @Operation(summary = "checkMobile", description = "检查手机号是否可用")
    public Result<Boolean> checkMobile(@RequestParam("mobile") String mobile) {
        Boolean available = authService.checkMobileAvailable(mobile);
        return Result.success(available);
    }

    @PostMapping("/bind-mobile")
    @Operation(summary = "bindMobile", description = "绑定手机号（验证码校验成功后更新用户手机号，支持通过userId参数或当前登录用户）")
    public Result<Boolean> bindMobile(@Valid @RequestBody BindMobileDTO bindMobileDTO) {
        Boolean result = authService.bindMobile(bindMobileDTO);
        return Result.success(result);
    }

}