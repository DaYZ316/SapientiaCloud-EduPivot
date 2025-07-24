package com.dayz.sapientiacloud_edupivot.auth.clients;

import com.dayz.sapientiacloud_edupivot.auth.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "SysUserClient", value = "SapientiaCloud-EduPivot--system", path = "/user", configuration = FeignConfig.class)
public interface SysUserClient {

    @GetMapping("/internal/info/{username}")
    Result<SysUserInternalVO> getUserInfoByUsername(@PathVariable("username") String username);

    @GetMapping("/internal/{userId}/role")
    Result<List<SysRoleVO>> getUserRoles(@PathVariable("userId") UUID userId);

    @PutMapping("/internal/update")
    Result<Boolean> updateUserInternal(@RequestBody SysUserDTO sysUserDTO);

    @PostMapping("/internal/register")
    Result<Boolean> registerUser(@RequestBody SysUserRegisterDTO sysUserRegisterDTO);

    @PutMapping("/internal/password")
    Result<Boolean> updatePassword(@RequestBody SysUserPasswordDTO sysUserPasswordDTO);
}