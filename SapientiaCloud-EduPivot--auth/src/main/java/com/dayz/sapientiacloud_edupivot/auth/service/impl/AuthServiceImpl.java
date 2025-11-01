package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserMobileLoginDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserRegisterDTO;
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
import com.dayz.sapientiacloud_edupivot.auth.service.VerificationCodeService;
import com.dayz.sapientiacloud_edupivot.auth.utils.EnumUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
    private final VerificationCodeService verificationCodeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserLoginVO login(SysUserLoginDTO sysUserLoginDTO) {
        if (sysUserLoginDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
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
        if (booleanResult == null || !booleanResult.isSuccess()) {
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
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND);
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
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND);
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        if (!StringUtils.hasText(username)) {
            throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND);
        }
        
        Result<SysUserInternalVO> result = sysUserClient.getUserInfoByUsername(username);
        if (result == null || !result.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        
        return result;
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

        // 第一步：先校验验证码（安全设计规范：验证码校验必须在最前面）
        verificationCodeService.verifyCode(
            sysUserMobileLoginDTO.getMobile(),
            sysUserMobileLoginDTO.getVerificationCode()
        );

        // 第二步：验证码校验通过后，才调用system模块查询用户
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

        // 生成JWT令牌
        String token = jwtUtil.generateToken(sysUserInternalVO);

        SysUserLoginVO loginVO = new SysUserLoginVO();
        loginVO.setAccessToken(token);
        BeanUtils.copyProperties(sysUserInternalVO, loginVO);

        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean register(SysUserRegisterDTO sysUserRegisterDTO) {
        if (sysUserRegisterDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserRegisterDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserRegisterDTO.getVerificationCode())) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        // 第一步：先校验验证码（安全设计规范：验证码校验必须在最前面）
        verificationCodeService.verifyCode(
            sysUserRegisterDTO.getMobile(), 
            sysUserRegisterDTO.getVerificationCode()
        );

        // 第二步：验证码校验通过后，才调用system模块进行用户注册
        Result<Boolean> result = sysUserClient.registerUser(sysUserRegisterDTO);
        if (result == null || !result.isSuccess()) {
            // 尝试根据返回的错误信息匹配对应的错误枚举
            if (result != null && StringUtils.hasText(result.getMessage())) {
                SysUserEnum sysUserEnum = EnumUtil.getByAttribute(
                    SysUserEnum.class, 
                    result.getMessage(), 
                    SysUserEnum::getMessage
                );
                if (sysUserEnum != null) {
                    throw new BusinessException(sysUserEnum);
                }
            }
            // 如果无法匹配具体错误，则抛出通用错误
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        Boolean registerResult = result.getData();
        if (registerResult == null || !registerResult) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        return registerResult;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkUsernameAvailable(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        
        // 用户名长度校验：4-20位（与注册时的校验保持一致）
        if (username.length() < 4 || username.length() > 20) {
            return false;
        }
        
        // 调用 system 模块检查用户名是否可用
        Result<Boolean> result = sysUserClient.checkUsernameAvailable(username);
        if (result == null || !result.isSuccess()) {
            // 如果调用失败，为了安全起见，返回 false（不可用）
            return false;
        }
        
        Boolean available = result.getData();
        return available != null && available;
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
                    throw new BusinessException(SysUserEnum.USER_LOGOUT_FAILED);
                }
            } else {
                throw new BusinessException(ResultEnum.TOKEN_NOT_FOUND);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(SysUserEnum.USER_LOGOUT_FAILED);
        }
    }
}