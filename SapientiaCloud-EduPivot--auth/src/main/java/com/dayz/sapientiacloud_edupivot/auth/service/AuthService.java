package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.RefreshTokenRequest;

import java.util.UUID;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}
