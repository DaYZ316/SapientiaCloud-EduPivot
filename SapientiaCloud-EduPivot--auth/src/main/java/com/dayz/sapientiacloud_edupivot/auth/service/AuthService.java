package com.dayz.sapientiacloud_edupivot.auth.service;

import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginRequest;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.LoginResponse;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.UserInfoResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

    UserInfoResponse getUserInfo(String username);
}
