package com.dayz.sapientiacloud_edupivot.course.common.clients;

import com.dayz.sapientiacloud_edupivot.course.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "PermissionClient", value = "SapientiaCloud-EduPivot--system", path = "/permission", configuration = FeignConfig.class)
public interface SysPermissionClient {

    @GetMapping("/internal/check/{permission}")
    Result<Boolean> hasPermission(@PathVariable("permission") String permission);

    @PostMapping("/internal/check/any")
    Result<Boolean> hasAnyPermission(@RequestBody List<String> permissions);

    @PostMapping("/internal/check/all")
    Result<Boolean> hasAllPermissions(@RequestBody List<String> permissions);

    @GetMapping("/internal/{userId}/permissions")
    Result<List<String>> getUserPermissions(@PathVariable("userId") UUID userId);

    @DeleteMapping("/internal/{userId}/cache")
    Result<Boolean> clearUserPermissionCache(@PathVariable("userId") UUID userId);

    @DeleteMapping("/internal/cache/all")
    Result<Boolean> clearAllPermissionCache();
}