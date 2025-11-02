package com.dayz.sapientiacloud_edupivot.system.feigns;

import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserPasswordDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysPermissionVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysRoleVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserInternalVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.ThirdPartyLoginResultVO;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "用户内部接口", description = "用户内部管理接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserFeign {

    private final ISysUserService sysUserService;

    @HasPermission(
            summary = "listAllSysUser",
            description = "获取所有用户列表。"
    )
    @GetMapping("internal/all")
    public Result<List<SysUserVO>> listAllSysUser() {
        List<SysUserVO> sysUserVOList = sysUserService.listAllSysUser();
        return Result.success(sysUserVOList);
    }

    @HasPermission(
            summary = "getUserRoles",
            description = "获取指定用户的角色列表"
    )
    @GetMapping("/internal/{userId}/role")
    public Result<List<SysRoleVO>> getUserRoles(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId
    ) {
        List<SysRoleVO> roles = sysUserService.getUserRoles(userId);
        return Result.success(roles);
    }

    @HasPermission(
            summary = "getUserPermissions",
            description = "获取指定用户的权限列表"
    )
    @GetMapping("/internal/{userId}/permission")
    public Result<List<SysPermissionVO>> getUserPermissions(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId
    ) {
        List<SysPermissionVO> permissions = sysUserService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @HasPermission(
            summary = "getUserInfoByUsername",
            description = "通过用户名获取用户详细信息"
    )
    @GetMapping("/internal/info/{username}")
    public Result<SysUserInternalVO> getUserInfoByUsername(@PathVariable("username") String username) {
        SysUserInternalVO sysUserInternalVO = sysUserService.selectUserByUsername(username);
        return Result.success(sysUserInternalVO);
    }

    @HasPermission(
            summary = "getUserInfoById",
            description = "通过用户ID获取用户详细信息"
    )
    @GetMapping("/internal/info/id/{userId}")
    public Result<SysUserInternalVO> getUserInfoById(@PathVariable("userId") UUID userId) {
        SysUserInternalVO sysUserInternalVO = sysUserService.getUserInfoById(userId);
        return Result.success(sysUserInternalVO);
    }


    @HasPermission(
            summary = "updateUserInternal",
            description = "内部接口，更新用户信息"
    )
    @PutMapping("/internal/update")
    public Result<Boolean> updateUserInternal(@RequestBody SysUserDTO sysUserDTO) {
        return Result.success(sysUserService.updateUser(sysUserDTO));
    }

    @HasPermission(
            summary = "registerUser",
            description = "用户UI端注册用户。"
    )
    @PostMapping("/internal/register")
    public Result<Boolean> registerUser(@RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return Result.success(sysUserService.registerUser(sysUserRegisterDTO));
    }

    @HasPermission(
            summary = "updatePassword",
            description = "更新用户密码"
    )
    @PutMapping("/internal/password")
    public Result<Boolean> updatePassword(@RequestBody SysUserPasswordDTO sysUserPasswordDTO) {
        return Result.success(sysUserService.updatePassword(sysUserPasswordDTO));
    }

    @HasPermission(
            summary = "updatePasswordByUserId",
            description = "通过用户ID更新密码"
    )
    @PutMapping("/internal/password/{userId}")
    public Result<Boolean> updatePasswordByUserId(
            @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable("userId") UUID userId,
            @Parameter(name = "newPassword", description = "新密码", required = true) @RequestParam("newPassword") String newPassword
    ) {
        return Result.success(sysUserService.updatePasswordByUserId(userId, newPassword));
    }

    @HasPermission(
            summary = "findOrCreateByThirdParty",
            description = "通过第三方账号查找或创建用户，返回注册状态"
    )
    @GetMapping("/internal/third-party/find-or-create")
    public Result<ThirdPartyLoginResultVO> findOrCreateByThirdParty(
            @RequestParam("provider") String provider,
            @RequestParam("providerId") String providerId,
            @RequestParam("username") String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "avatarUrl", required = false) String avatarUrl
    ) {
        return Result.success(sysUserService.findOrCreateByThirdParty(provider, providerId, username, email, name, avatarUrl));
    }

    @HasPermission(
            summary = "checkUsernameAvailable",
            description = "检查用户名是否可用"
    )
    @GetMapping("/internal/check-username")
    public Result<Boolean> checkUsernameAvailable(
            @Parameter(name = "username", description = "用户名", required = true) 
            @RequestParam("username") String username
    ) {
        Boolean available = sysUserService.isUsernameAvailable(username);
        return Result.success(available);
    }

    @HasPermission(
            summary = "checkMobileAvailable",
            description = "检查手机号是否可用"
    )
    @GetMapping("/internal/check-mobile")
    public Result<Boolean> checkMobileAvailable(
            @Parameter(name = "mobile", description = "手机号", required = true) 
            @RequestParam("mobile") String mobile
    ) {
        Boolean exists = sysUserService.isMobileExists(mobile);
        // isMobileExists 返回 true 表示已被使用，所以 available = !exists
        Boolean available = !exists;
        return Result.success(available);
    }
}
