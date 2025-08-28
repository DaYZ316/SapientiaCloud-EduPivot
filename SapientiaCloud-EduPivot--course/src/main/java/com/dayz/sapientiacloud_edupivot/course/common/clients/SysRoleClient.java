package com.dayz.sapientiacloud_edupivot.course.common.clients;

import com.dayz.sapientiacloud_edupivot.course.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(contextId = "SysRoleClient", value = "SapientiaCloud-EduPivot--system", path = "/role", configuration = FeignConfig.class)
public interface SysRoleClient {

    @GetMapping("/internal/key/{roleKey}")
    Result<SysRoleVO> getRoleByKey(@PathVariable("roleKey") String roleKey);

    @PostMapping("/internal/user/{userId}/role")
    Result<Boolean> addRoleToUser(@PathVariable("userId") UUID userId, @RequestParam("roleKey") String roleKey);

    @DeleteMapping("/internal/user/{userId}/role")
    Result<Boolean> removeRoleFromUser(@PathVariable("userId") UUID userId, @RequestParam("roleKey") String roleKey);
}
