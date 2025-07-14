package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.client.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserExceptionEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.result.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserClient sysUserClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public SysUserLoginVO login(SysUserLoginDTO loginDTO) {
        // 参数校验
        if (loginDTO.getUsername() == null || loginDTO.getUsername().isEmpty()) {
            throw new BusinessException(SysUserExceptionEnum.USERNAME_CANNOT_BE_EMPTY.getCode(), 
                                       SysUserExceptionEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            throw new BusinessException(SysUserExceptionEnum.PASSWORD_CANNOT_BE_EMPTY.getCode(), 
                                       SysUserExceptionEnum.PASSWORD_CANNOT_BE_EMPTY.getMessage());
        }

        // 获取用户信息
        Result<SysUser> userResult = sysUserClient.getUserInfoByUsername(loginDTO.getUsername());
        if (!userResult.isSuccess() || userResult.getData() == null) {
            log.error("用户登录失败: 用户不存在, 用户名: {}", loginDTO.getUsername());
            throw new BusinessException(SysUserExceptionEnum.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        
        SysUser user = userResult.getData();
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 1) {
            log.error("用户登录失败: 用户已被禁用, 用户名: {}", loginDTO.getUsername());
            throw new BusinessException(SysUserExceptionEnum.USER_ACCOUNT_DISABLED.getMessage());
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.error("用户登录失败: 密码错误, 用户名: {}", loginDTO.getUsername());
            throw new BusinessException(SysUserExceptionEnum.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user);
        
        // 构建响应对象，添加完整用户信息
        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickName(user.getNickName());
        
        log.info("用户登录成功: 用户名: {}", loginDTO.getUsername());
        return loginVO;
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            return !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return processLogout(token);
    }
    
    @Override
    public boolean logout(HttpServletRequest request, String token) {
        // 优先使用请求参数中的token，如果为空则尝试从请求头中提取
        String tokenToUse = StringUtils.hasText(token) ? token : extractTokenFromRequest(request);
        return processLogout(tokenToUse);
    }
    
    /**
     * 处理登出逻辑
     * 
     * @param token JWT令牌
     * @return 是否成功登出
     */
    private boolean processLogout(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND.getMessage());
        }
        
        // 验证令牌有效性
        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultEnum.TOKEN_EXPIRED.getMessage());
            }
            
            // 获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                // 将令牌加入黑名单
                boolean invalidated = jwtUtil.invalidateToken(token);
                if (invalidated) {
                    log.info("用户 {} 已成功登出", username);
                    return true;
                } else {
                    log.error("用户 {} 登出失败: 无法使令牌失效", username);
                    return false;
                }
            } else {
                log.error("登出失败: 无法从令牌获取用户信息");
                return false;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登出过程发生错误", e);
            throw new BusinessException("登出失败，请稍后再试");
        }
    }
    
    /**
     * 从请求中提取JWT令牌
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头中获取令牌
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken;
        }
        return null;
    }
} 