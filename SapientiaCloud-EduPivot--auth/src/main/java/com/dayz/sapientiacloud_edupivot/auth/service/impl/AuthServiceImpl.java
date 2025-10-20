package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserMobileLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserLoginVO;
import com.dayz.sapientiacloud_edupivot.auth.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.JwtUtil;
import com.dayz.sapientiacloud_edupivot.auth.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final static String INIT_VERIFICATION_CODE = "123456";

    private final SysUserClient sysUserClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO login(SysUserLoginDTO sysUserLoginDTO) {
        if (sysUserLoginDTO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        if (!StringUtils.hasText(sysUserLoginDTO.getUsername())) {
            throw new BusinessException(SysUserEnum.USERNAME_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserLoginDTO.getPassword())) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }

        Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoByUsername(sysUserLoginDTO.getUsername());
        if (userResult == null || !userResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserInternalVO sysUserInternalVO = userResult.getData();
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USERNAME_OR_PASSWORD_ERROR);
        }
        if (sysUserInternalVO.getStatus() != null && sysUserInternalVO.getStatus() == StatusEnum.DISABLED.getCode()) {
            throw new BusinessException(SysUserEnum.USER_ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(sysUserLoginDTO.getPassword(), sysUserInternalVO.getPassword())) {
            throw new BusinessException(SysUserEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        sysUserInternalVO.setLastLoginTime(LocalDateTime.now());

        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(sysUserInternalVO, sysUserDTO);
        Result<Boolean> booleanResult = sysUserClient.updateUserInternal(sysUserDTO);
        if (!booleanResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_LOGIN_FAILED);
        }

        String token = jwtUtil.generateToken(sysUserInternalVO);

        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        BeanUtils.copyProperties(sysUserInternalVO, loginVO);

        return loginVO;
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            return !jwtUtil.isTokenExpired(token);
        } catch (Exception e) {
            throw new BusinessException("令牌验证失败: " + e.getMessage());
        }
    }

    @Override
    public boolean logout(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        return processLogout(token);
    }

    @Override
    public Result<SysUserInternalVO> getUserInfo(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        return sysUserClient.getUserInfoByUsername(jwtUtil.getUsernameFromToken(token));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePassword(HttpServletRequest request, SysUserPasswordDTO sysUserPasswordDTO) {
        if (sysUserPasswordDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        if (!sysUserPasswordDTO.getNewPassword().equals(sysUserPasswordDTO.getConfirmPassword())) {

            throw new BusinessException(SysUserEnum.NEW_AND_CONFIRM_PASSWORD_NOT_MATCH);
        }
        if (sysUserPasswordDTO.getCurrentPassword().equals(sysUserPasswordDTO.getNewPassword())) {
            throw new BusinessException(SysUserEnum.NEW_PASSWORD_SAME_AS_CURRENT_PASSWORD);
        }

        SysUserInternalVO currentUser = UserContextUtil.getCurrentUser();

        if (!passwordEncoder.matches(sysUserPasswordDTO.getCurrentPassword(), currentUser.getPassword())) {
            throw new BusinessException(SysUserEnum.CURRENT_PASSWORD_NOT_MATCH);
        }

        Result<Boolean> result = sysUserClient.updatePassword(sysUserPasswordDTO);
        if (result == null || !result.isSuccess()) {
            throw new BusinessException(SysUserEnum.PASSWORD_UPDATE_FAILED);
        }

        return this.logout(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO mobileLogin(SysUserMobileLoginDTO sysUserMobileLoginDTO) {
        if (sysUserMobileLoginDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobileLoginDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobileLoginDTO.getVerificationCode())) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        // 调用system模块的手机验证码登录接口
        Result<SysUserInternalVO> userResult = sysUserClient.mobileLogin(sysUserMobileLoginDTO);
        if (userResult == null || !userResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_LOGIN_FAILED);
        }

        SysUserInternalVO sysUserInternalVO = userResult.getData();
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        if (sysUserInternalVO.getStatus() != null && sysUserInternalVO.getStatus() == StatusEnum.DISABLED.getCode()) {
            throw new BusinessException(SysUserEnum.USER_ACCOUNT_DISABLED);
        }

        if (!sysUserMobileLoginDTO.getVerificationCode().equals(INIT_VERIFICATION_CODE)) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_ERROR);
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(sysUserInternalVO);

        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        BeanUtils.copyProperties(sysUserInternalVO, loginVO);

        return loginVO;
    }

    private boolean processLogout(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND);
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ResultEnum.TOKEN_EXPIRED);
            }

            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                boolean invalidated = jwtUtil.invalidateToken(token);
                if (invalidated) {
                    return true;
                } else {
                    throw new BusinessException("用户 " + username + " 登出失败: 无法使令牌失效");
                }
            } else {
                throw new BusinessException("登出失败: 无法从令牌获取用户信息");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("登出过程发生错误: " + e.getMessage());
        }
    }
}