package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    SysUserLoginVO login(String username, String password);

    boolean validateToken(String token);

    boolean logout(HttpServletRequest request);

    boolean logout(HttpServletRequest request, String token);
}