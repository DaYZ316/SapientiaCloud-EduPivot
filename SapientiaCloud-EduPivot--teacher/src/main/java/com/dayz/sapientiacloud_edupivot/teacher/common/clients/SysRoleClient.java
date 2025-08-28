package com.dayz.sapientiacloud_edupivot.teacher.common.clients;

import com.dayz.sapientiacloud_edupivot.teacher.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.teacher.common.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import io.swagger.v3.oas.annotations.Parameter;
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
