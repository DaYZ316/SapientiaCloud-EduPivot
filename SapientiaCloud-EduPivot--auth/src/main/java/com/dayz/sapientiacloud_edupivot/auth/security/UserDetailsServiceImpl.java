package com.dayz.sapientiacloud_edupivot.auth.security;

import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.auth.enums.SysUserExceptionEnum;
import com.dayz.sapientiacloud_edupivot.auth.client.SysUserClient;
import com.dayz.sapientiacloud_edupivot.auth.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserClient sysUserClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Result<SysUser> userResult = sysUserClient.getUserInfoByUsername(username);

        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new UsernameNotFoundException(SysUserExceptionEnum.USER_NOT_FOUND.getMessage());
        }

        SysUser sysUser = userResult.getData();
        return new User(sysUser.getUsername(), sysUser.getPassword(), new ArrayList<>());
    }
}