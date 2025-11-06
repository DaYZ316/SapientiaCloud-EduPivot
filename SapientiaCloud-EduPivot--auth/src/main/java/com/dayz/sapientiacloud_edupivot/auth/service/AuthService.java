package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    SysUserLoginVO login(SysUserLoginDTO sysUserLoginDTO);

    boolean validateToken(String token);

    boolean logout(HttpServletRequest request);

    Result<SysUserInternalVO> getUserInfo(HttpServletRequest httpServletRequest);

    Boolean updatePassword(HttpServletRequest request, SysUserPasswordDTO sysUserPasswordDTO);

    Boolean updatePasswordByMobile(SysUserMobilePasswordDTO sysUserMobilePasswordDTO);

    SysUserLoginVO mobileLogin(SysUserMobileLoginDTO sysUserMobileLoginDTO);

    Boolean register(SysUserRegisterDTO sysUserRegisterDTO);

    Boolean checkUsernameAvailable(String username);

    BindMobileResultDTO checkMobileAvailable(String mobile);

    BindMobileResultDTO bindMobile(BindMobileDTO bindMobileDTO);

    BindMobileResultDTO bindMobileConfirm(BindMobileConfirmDTO bindMobileConfirmDTO);
}