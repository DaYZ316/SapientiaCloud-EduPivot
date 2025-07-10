package com.dayz.sapientiacloud_edupivot.auth.service.impl;

import com.dayz.sapientiacloud_edupivot.auth.client.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.UserInfoResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserExceptionEnum;
import com.dayz.sapientiacloud_edupivot.auth.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import com.dayz.sapientiacloud_edupivot.auth.service.AuthService;
import com.dayz.sapientiacloud_edupivot.auth.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import com.dayz.sapientiacloud_edupivot.auth.config.JwtProperties;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserClient sysUserClient;
    private final JwtProperties jwtProperties;

    private static final String TOKEN_TYPE = "Bearer";

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 1. 验证用户名和密码
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. 获取用户信息
        Result<SysUser> userResult = sysUserClient.getUserInfoByUsername(loginRequest.getUsername());
        if (!userResult.isSuccess() || Objects.isNull(userResult.getData())) {
            throw new BusinessException(SysUserExceptionEnum.USER_LOGIN_FAILED.getMessage());
        }
        SysUser sysUser = userResult.getData();

        // 3. 生成访问令牌
        String accessToken = jwtUtil.generateToken(sysUser.getId(), sysUser.getUsername());

        return new LoginResponse(
                accessToken,
                TOKEN_TYPE,
                jwtProperties.getExpiration(),
                sysUser.getId(),
                sysUser.getUsername()
        );
    }

    @Override
    public UserInfoResponse getUserInfo(String username) {
        Result<SysUser> userResult = sysUserClient.getUserInfoByUsername(username);
        if (!userResult.isSuccess() || Objects.isNull(userResult.getData())) {
            throw new BusinessException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }
        SysUser sysUser = userResult.getData();
        return UserInfoResponse.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .nickname(sysUser.getNickName())
                .avatar(sysUser.getAvatar())
                .build();
    }
}
