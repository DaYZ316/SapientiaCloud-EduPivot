package com.dayz.sapientiacloud_edupivot.system.feigns;

import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "角色内部接口", description = "角色内部管理接口")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Slf4j
public class SysRoleFeign {

    private final ISysRoleService sysRoleService;

    /**
     * 根据角色标识获取角色信息
     *
     * @param roleKey 角色标识
     * @return 角色信息
     */
    @HasPermission(
            summary = "内部接口 - 根据角色标识获取角色信息",
            description = "通过角色标识获取角色详细信息，包含权限列表"
    )
    @GetMapping("/internal/key/{roleKey}")
    public Result<SysRoleVO> getRoleByKey(
            @Parameter(name = "roleKey", description = "角色标识", required = true) @PathVariable("roleKey") String roleKey
    ) {
        SysRoleVO sysRoleVO = sysRoleService.getRoleByKey(roleKey);
        return Result.success(sysRoleVO);
    }

    /**
     * 为指定用户添加指定角色
     *
     * @param userId  用户ID
     * @param roleKey 角色标识
     * @return 操作结果
     */
    @HasPermission(
            summary = "内部接口 - 为指定用户添加指定角色",
            description = "通过用户ID和角色标识为用户分配角色"
    )
    @PostMapping("/internal/user/{userId}/role")
    public Result<Boolean> addRoleToUser(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId,
            @Parameter(name = "roleKey", description = "角色标识", required = true) @RequestParam("roleKey") String roleKey
    ) {
        Boolean result = sysRoleService.addRoleToUser(userId, roleKey);
        return Result.success(result);
    }

    /**
     * 从指定用户中删除指定角色
     *
     * @param userId  用户ID
     * @param roleKey 角色标识
     * @return 操作结果
     */
    @HasPermission(
            summary = "内部接口 - 从指定用户中删除指定角色",
            description = "通过用户ID和角色标识从用户中移除角色"
    )
    @DeleteMapping("/internal/user/{userId}/role")
    public Result<Boolean> removeRoleFromUser(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId,
            @Parameter(name = "roleKey", description = "角色标识", required = true) @RequestParam("roleKey") String roleKey
    ) {
        Boolean result = sysRoleService.removeRoleFromUser(userId, roleKey);
        return Result.success(result);
    }
}
