package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RefreshTokenRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RegisterRequest;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    Object getUserInfo(Long userId);
}
