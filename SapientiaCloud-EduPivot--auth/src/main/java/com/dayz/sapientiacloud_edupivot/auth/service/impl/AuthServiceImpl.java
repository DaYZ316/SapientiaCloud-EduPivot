package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.*;
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
        // 检查数据库中存储的密码是否为空，避免触发 BCryptPasswordEncoder 的警告
        if (!StringUtils.hasText(sysUserInternalVO.getPassword())) {
            throw new BusinessException(SysUserEnum.USERNAME_OR_PASSWORD_ERROR);
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
        // 在返回之前将密码字段置空，避免密码泄露
        loginVO.setPassword(null);

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

        if (!StringUtils.hasText(currentUser.getPassword())) {
            throw new BusinessException(SysUserEnum.CURRENT_PASSWORD_NOT_MATCH);
        }
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
    public Boolean updatePasswordByMobile(SysUserMobilePasswordDTO sysUserMobilePasswordDTO) {
        if (sysUserMobilePasswordDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobilePasswordDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobilePasswordDTO.getVerificationCode())) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobilePasswordDTO.getNewPassword())) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(sysUserMobilePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(SysUserEnum.PASSWORD_CANNOT_BE_EMPTY);
        }

        // 验证新密码和确认密码是否一致
        if (!sysUserMobilePasswordDTO.getNewPassword().equals(sysUserMobilePasswordDTO.getConfirmPassword())) {
            throw new BusinessException(SysUserEnum.NEW_AND_CONFIRM_PASSWORD_NOT_MATCH);
        }

        // 第一步：先校验验证码（安全设计规范：验证码校验必须在最前面）
        verificationCodeService.verifyCode(
                sysUserMobilePasswordDTO.getMobile(),
                sysUserMobilePasswordDTO.getVerificationCode()
        );

        // 第二步：根据手机号查找用户（验证码已在前一步验证，这里仅需传入手机号）
        SysUserMobileLoginDTO mobileLoginDTO = new SysUserMobileLoginDTO();
        mobileLoginDTO.setMobile(sysUserMobilePasswordDTO.getMobile());
        // 验证码字段仍然需要传入，但 system 模块不会再次验证（已在注释中说明）
        mobileLoginDTO.setVerificationCode(sysUserMobilePasswordDTO.getVerificationCode());

        Result<SysUserInternalVO> userResult = sysUserClient.mobileLogin(mobileLoginDTO);
        if (userResult == null || !userResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        SysUserInternalVO sysUserInternalVO = userResult.getData();
        if (sysUserInternalVO == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }

        // 验证新密码不能和旧密码一样
        if (StringUtils.hasText(sysUserInternalVO.getPassword())) {
            if (passwordEncoder.matches(sysUserMobilePasswordDTO.getNewPassword(), sysUserInternalVO.getPassword())) {
                throw new BusinessException(SysUserEnum.NEW_PASSWORD_SAME_AS_CURRENT_PASSWORD);
            }
        }

        // 第三步：通过用户ID更新密码
        Result<Boolean> result = sysUserClient.updatePasswordByUserId(
                sysUserInternalVO.getId(),
                sysUserMobilePasswordDTO.getNewPassword()
        );
        if (result == null || !result.isSuccess()) {
            throw new BusinessException(SysUserEnum.PASSWORD_UPDATE_FAILED);
        }

        return true;
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
        // 在返回之前将密码字段置空，避免密码泄露
        loginVO.setPassword(null);

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

    @Override
    @Transactional(readOnly = true)
    public Boolean checkMobileAvailable(String mobile) {
        if (!StringUtils.hasText(mobile)) {
            return false;
        }

        // 手机号格式校验：11位数字，以1开头
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            return false;
        }

        // 调用 system 模块检查手机号是否可用
        Result<Boolean> result = sysUserClient.checkMobileAvailable(mobile);
        if (result == null || !result.isSuccess()) {
            // 如果调用失败，为了安全起见，返回 false（不可用）
            return false;
        }

        Boolean available = result.getData();
        return available != null && available;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindMobile(BindMobileDTO bindMobileDTO) {
        if (bindMobileDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(bindMobileDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(bindMobileDTO.getVerificationCode())) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        // 第一步：先校验验证码（安全设计规范：验证码校验必须在最前面）
        verificationCodeService.verifyCode(
                bindMobileDTO.getMobile(),
                bindMobileDTO.getVerificationCode()
        );

        // 第二步：获取目标用户信息（支持通过userId或当前登录用户）
        SysUserInternalVO targetUser = null;

        if (bindMobileDTO.getUserId() != null) {
            // 如果提供了userId，通过userId获取用户信息（用于第三方登录场景）
            Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoById(bindMobileDTO.getUserId());
            if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
            targetUser = userResult.getData();
        } else {
            // 如果没有提供userId，尝试从当前登录用户获取（兼容原有逻辑）
            try {
                targetUser = UserContextUtil.getCurrentUser();
            } catch (Exception e) {
                // 如果获取当前用户失败，说明用户未登录且未提供userId
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
        }

        // 第三步：如果目标用户已经绑定该手机号，直接返回成功
        if (StringUtils.hasText(targetUser.getMobile()) && targetUser.getMobile().equals(bindMobileDTO.getMobile())) {
            return true;
        }

        // 第四步：检查手机号是否已被其他用户使用
        Result<Boolean> checkResult = sysUserClient.checkMobileAvailable(bindMobileDTO.getMobile());
        if (checkResult == null || !checkResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }
        Boolean available = checkResult.getData();
        if (available == null || !available) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_ALREADY_EXISTS);
        }

        // 第五步：构建更新DTO（保留其他信息，只更新手机号）
        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(targetUser, sysUserDTO);
        sysUserDTO.setMobile(bindMobileDTO.getMobile());

        // 第六步：调用system模块更新用户手机号
        Result<Boolean> updateResult = sysUserClient.updateUserInternal(sysUserDTO);
        if (updateResult == null || !updateResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        Boolean updateSuccess = updateResult.getData();
        if (updateSuccess == null || !updateSuccess) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        return true;
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