package com.dayz.sapientiacloud_edupivot.auth.client;

import com.dayz.sapientiacloud_edupivot.auth.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(contextId = "SysUserClient", value = "SapientiaCloud-EduPivot--system", path = "/user")
public interface SysUserClient {
    @GetMapping("/internal/info/{username}")
    Result<SysUser> getUserInfoByUsername(@PathVariable("username") String username);
}