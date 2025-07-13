package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    SysUserLoginVO login(SysUserLoginDTO loginDTO);

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
    
    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 是否成功登出
     */
    boolean logout(HttpServletRequest request);
    
    /**
     * 用户登出（支持从请求参数获取token）
     *
     * @param request HTTP请求
     * @param token 请求参数中的令牌（可为空）
     * @return 是否成功登出
     */
    boolean logout(HttpServletRequest request, String token);
}