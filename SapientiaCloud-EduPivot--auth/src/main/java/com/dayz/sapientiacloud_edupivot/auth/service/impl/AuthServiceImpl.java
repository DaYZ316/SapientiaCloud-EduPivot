package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.client.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserInternalDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.result.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.security.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserClient sysUserClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO login(SysUserLoginDTO sysUserLoginDTO) {
        if (!StringUtils.hasText(sysUserLoginDTO.getUsername())) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY.getMessage());
        }
        if (!StringUtils.hasText(sysUserLoginDTO.getPassword())) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY.getMessage());
        }

        Result<SysUserInternalDTO> userResult = sysUserClient.getUserInfoByUsername(sysUserLoginDTO.getUsername());
        if (userResult == null || !userResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND.getMessage());
        }
        SysUserInternalDTO sysUserInternalDTO = userResult.getData();
        if (sysUserInternalDTO == null) {
            log.error("用户登录失败: 用户不存在, 用户名: {}", sysUserLoginDTO.getUsername());
            throw new BusinessException(SysUserEnum.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }
        if (sysUserInternalDTO.getStatus() != null && sysUserInternalDTO.getStatus() == 1) {
            log.error("用户登录失败: 用户已被禁用, 用户名: {}", sysUserLoginDTO.getUsername());
            throw new BusinessException(SysUserEnum.USER_ACCOUNT_DISABLED.getMessage());
        }
        if (!passwordEncoder.matches(sysUserLoginDTO.getPassword(), sysUserInternalDTO.getPassword())) {
            log.error("用户登录失败: 密码错误, 用户名: {}", sysUserLoginDTO.getUsername());
            throw new BusinessException(SysUserEnum.USERNAME_OR_PASSWORD_ERROR.getMessage());
        }

        sysUserInternalDTO.setLastLoginTime(LocalDateTime.now());

        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(sysUserInternalDTO, sysUserDTO);
        Result<Boolean> booleanResult = sysUserClient.updateUserInternal(sysUserDTO);
        if (!booleanResult.isSuccess()) {
            log.error("用户登录失败: 更新用户信息失败, 用户名: {}", sysUserLoginDTO.getUsername());
            throw new BusinessException(SysUserEnum.USER_LOGIN_FAILED.getMessage());
        }

        String token = jwtUtil.generateToken(sysUserInternalDTO);

        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        BeanUtils.copyProperties(sysUserInternalDTO, loginVO);
        
        log.info("用户登录成功: 用户名: {}", sysUserInternalDTO.getUsername());
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
        String tokenToUse = StringUtils.hasText(token) ? token : extractTokenFromRequest(request);
        return processLogout(tokenToUse);
    }

    private boolean processLogout(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND.getMessage());
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultEnum.TOKEN_EXPIRED.getMessage());
            }

            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
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

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken;
        }
        return null;
    }
} 