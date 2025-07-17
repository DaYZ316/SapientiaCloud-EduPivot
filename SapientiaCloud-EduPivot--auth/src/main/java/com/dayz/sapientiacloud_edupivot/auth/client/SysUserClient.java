package com.dayz.sapientiacloud_edupivot.auth.client;

import com.dayz.sapientiacloud_edupivot.auth.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.auth.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "SysUserClient", value = "SapientiaCloud-EduPivot--system", path = "/user", configuration = FeignConfig.class)
public interface SysUserClient {

    @GetMapping("/internal/info/{username}")
    Result<SysUser> getUserInfoByUsername(@PathVariable("username") String username);

    @GetMapping("/internal/{userId}/role")
    Result<List<SysRoleVO>> getUserRoles(@PathVariable("userId") UUID userId);

    @PutMapping("/internal/update")
    Result<Boolean> updateUserInternal(@RequestBody SysUserDTO sysUserDTO);
}