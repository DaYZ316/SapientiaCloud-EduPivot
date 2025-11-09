package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserBasicInfoVO;
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
    public BindMobileResultDTO checkMobileAvailable(String mobile) {
        BindMobileResultDTO result = new BindMobileResultDTO();

        // 1. 基本校验
        if (!StringUtils.hasText(mobile)) {
            result.setSuccess(false);
            result.setNeedConfirm(false);
            return result;
        }

        // 2. 手机号格式校验：11位数字，以1开头
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            result.setSuccess(false);
            result.setNeedConfirm(false);
            return result;
        }

        // 3. 调用 system 模块检查手机号是否可用
        Result<Boolean> checkResult = sysUserClient.checkMobileAvailable(mobile);
        if (checkResult == null || !checkResult.isSuccess()) {
            // 如果调用失败，为了安全起见，返回不可用
            result.setSuccess(false);
            result.setNeedConfirm(false);
            return result;
        }

        Boolean available = checkResult.getData();

        // 4. 如果手机号可用
        if (available != null && available) {
            result.setSuccess(true);
            result.setNeedConfirm(false);
            result.setExistingUserInfo(null);
            return result;
        }

        // 5. 如果手机号已被使用，获取已存在用户信息
        Result<SysUserBasicInfoVO> existingUserResult = sysUserClient.getUserInfoByMobile(mobile);
        if (existingUserResult != null && existingUserResult.isSuccess() && existingUserResult.getData() != null) {
            result.setSuccess(false);
            result.setNeedConfirm(true);
            result.setExistingUserInfo(existingUserResult.getData());
            return result;
        }

        // 6. 如果获取已存在用户信息失败，返回不可用但不需确认
        result.setSuccess(false);
        result.setNeedConfirm(false);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindMobileResultDTO bindMobile(BindMobileDTO bindMobileDTO) {
        if (bindMobileDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(bindMobileDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(bindMobileDTO.getVerificationCode())) {
            throw new BusinessException(SysUserEnum.VERIFICATION_CODE_CANNOT_BE_EMPTY);
        }

        verificationCodeService.verifyCode(
                bindMobileDTO.getMobile(),
                bindMobileDTO.getVerificationCode()
        );

        SysUserInternalVO targetUser = null;

        if (bindMobileDTO.getUserId() != null) {
            Result<SysUserInternalVO> userResult = sysUserClient.getUserInfoById(bindMobileDTO.getUserId());
            if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
            targetUser = userResult.getData();
        } else {
            try {
                targetUser = UserContextUtil.getCurrentUser();
            } catch (Exception e) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }
        }

        if (StringUtils.hasText(targetUser.getMobile()) && targetUser.getMobile().equals(bindMobileDTO.getMobile())) {
            BindMobileResultDTO result = new BindMobileResultDTO();
            result.setSuccess(true);
            result.setNeedConfirm(false);
            return result;
        }

        // 检查手机号是否可用
        BindMobileResultDTO checkResult = this.checkMobileAvailable(bindMobileDTO.getMobile());
        if (!checkResult.getSuccess()) {
            if (checkResult.getNeedConfirm() != null && checkResult.getNeedConfirm()) {
                // 手机号已被使用，返回需要确认
                return checkResult;
            } else {
                // 手机号格式错误或其他错误
                throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
            }
        }

        SysUserDTO sysUserDTO = new SysUserDTO();
        BeanUtils.copyProperties(targetUser, sysUserDTO);
        sysUserDTO.setMobile(bindMobileDTO.getMobile());

        Result<Boolean> updateResult = sysUserClient.updateUserInternal(sysUserDTO);
        if (updateResult == null || !updateResult.isSuccess()) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        Boolean updateSuccess = updateResult.getData();
        if (updateSuccess == null || !updateSuccess) {
            throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
        }

        BindMobileResultDTO result = new BindMobileResultDTO();
        result.setSuccess(true);
        result.setNeedConfirm(false);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindMobileResultDTO bindMobileConfirm(BindMobileConfirmDTO bindMobileConfirmDTO) {
        if (bindMobileConfirmDTO == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }
        if (!StringUtils.hasText(bindMobileConfirmDTO.getMobile())) {
            throw new BusinessException(SysUserEnum.PHONE_NUMBER_CANNOT_BE_EMPTY);
        }
        if (bindMobileConfirmDTO.getUserId() == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        if (bindMobileConfirmDTO.getIsSameAccount() == null) {
            throw new BusinessException(SysUserEnum.DATA_CANNOT_BE_EMPTY);
        }

        Result<SysUserInternalVO> tempUserResult = sysUserClient.getUserInfoById(bindMobileConfirmDTO.getUserId());
        if (tempUserResult == null || !tempUserResult.isSuccess() || tempUserResult.getData() == null) {
            throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
        }
        SysUserInternalVO tempUser = tempUserResult.getData();

        if (bindMobileConfirmDTO.getIsSameAccount()) {
            Result<SysUserBasicInfoVO> existingUserResult = sysUserClient.getUserInfoByMobile(bindMobileConfirmDTO.getMobile());
            if (existingUserResult == null || !existingUserResult.isSuccess() || existingUserResult.getData() == null) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }

            if (!StringUtils.hasText(tempUser.getGithubId())) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            Result<Boolean> updateGithubResult = sysUserClient.updateGithubId(
                    existingUserResult.getData().getId(),
                    tempUser.getGithubId()
            );
            if (updateGithubResult == null || !updateGithubResult.isSuccess()) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            Result<Boolean> removeResult = sysUserClient.physicalDeleteUserById(bindMobileConfirmDTO.getUserId());
            if (removeResult == null || !removeResult.isSuccess() || !Boolean.TRUE.equals(removeResult.getData())) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            Result<SysUserInternalVO> mergedUserResult = sysUserClient.getUserInfoById(existingUserResult.getData().getId());
            if (mergedUserResult == null || !mergedUserResult.isSuccess() || mergedUserResult.getData() == null) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            BindMobileResultDTO result = new BindMobileResultDTO();
            result.setSuccess(true);
            result.setNeedConfirm(false);
            return result;
        } else {
            Result<SysUserBasicInfoVO> existingUserResult = sysUserClient.getUserInfoByMobile(bindMobileConfirmDTO.getMobile());
            if (existingUserResult == null || !existingUserResult.isSuccess() || existingUserResult.getData() == null) {
                throw new BusinessException(SysUserEnum.USER_NOT_FOUND);
            }

            Result<Boolean> softDeleteResult = sysUserClient.softDeleteUserById(existingUserResult.getData().getId());
            if (softDeleteResult == null || !softDeleteResult.isSuccess()) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            SysUserDTO sysUserDTO = new SysUserDTO();
            BeanUtils.copyProperties(tempUser, sysUserDTO);
            sysUserDTO.setMobile(bindMobileConfirmDTO.getMobile());

            Result<Boolean> updateResult = sysUserClient.updateUserInternal(sysUserDTO);
            if (updateResult == null || !updateResult.isSuccess()) {
                throw new BusinessException(SysUserEnum.USER_SERVICE_ERROR);
            }

            BindMobileResultDTO result = new BindMobileResultDTO();
            result.setSuccess(true);
            result.setNeedConfirm(false);
            return result;
        }
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