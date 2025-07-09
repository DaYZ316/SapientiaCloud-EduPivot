package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LogoutDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.LogoutVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 智语·云枢认证服务核心接口
 *
 * @author DaYZ
 * @version 1.0.0
 * @since 2025-01-08
 */
public interface AuthService {

    /**
     * 用户登录接口
     *
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌字符串
     */
    String login(String username, String password);

    /**
     * 用户登录认证接口
     *
     * @param token JWT令牌字符串
     * @return true表示令牌有效，false表示令牌无效
     */
    boolean validateToken(String token);

    /**
     * 增强用户登出接口
     * 
     * @param request HTTP请求对象，用于获取Authorization头中的JWT令牌
     * @param logoutDTO 登出请求参数，包含登出类型、原因等信息（可选）
     * @return 登出响应信息，包含登出用户、时间、设备数量等详细信息
     */
    LogoutVO logoutEnhanced(HttpServletRequest request, LogoutDTO logoutDTO);

    /**
     * 带令牌参数的用户登出接口
     * 
     * @param token JWT令牌字符串，要登出的目标令牌
     * @param logoutDTO 登出请求参数，包含登出类型和原因等信息（可选）
     * @param clientIp 客户端IP地址，用于安全审计和日志记录
     * @return 登出响应信息，包含登出详细信息
     */
    LogoutVO logoutWithToken(String token, LogoutDTO logoutDTO, String clientIp);

    /**
     * 管理员强制登出用户所有设备接口
     *
     * @param username 目标用户名，要强制登出的用户账户
     * @param reason 强制登出的原因，用于审计日志记录
     * @return 失效的令牌数量，表示撤销了多少个有效令牌
     */
    int forceLogoutAllDevices(String username, String reason);
} 