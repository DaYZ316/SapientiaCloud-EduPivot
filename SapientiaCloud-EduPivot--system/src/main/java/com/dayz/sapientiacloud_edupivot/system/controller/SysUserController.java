package com.dayz.sapientiacloud_edupivot.system.controller;

import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserAdminDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.SysUserRegisterDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.system.result.Result;
import com.dayz.sapientiacloud_edupivot.system.service.ISysUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "用户管理", description = "用于管理系统用户的API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;

    @Operation(
            summary = "分页查找用户",
            description = "根据传入的条件分页查询用户信息。支持根据用户名、昵称等字段进行模糊查询。"
    )
    @GetMapping("/list")
    public Result<PageInfo<SysUserVO>> listSysUser(@ParameterObject SysUserQueryDTO sysUserQueryDTO) {
        PageInfo<SysUserVO> list = sysUserService.listSysUser(sysUserQueryDTO);
        return Result.success(list);
    }

    @Operation(summary = "根据ID获取用户", description = "通过用户的唯一ID获取其详细信息。")
    @GetMapping("/{id}")
    public Result<SysUserVO> getUserById(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        SysUserVO sysUserVO = sysUserService.getUserById(id);
        return Result.success(sysUserVO);
    }

    @Operation(summary = "管理员添加新用户", description = "管理员向系统中添加一个新用户。")
    @PostMapping
    public Result<SysUserVO> addUser(@Valid @RequestBody SysUserAdminDTO sysUserAdminDTO) {
        return Result.success(sysUserService.addUser(sysUserAdminDTO));
    }

    @Operation(summary = "更新现有用户", description = "修改现有用户的信息。")
    @PutMapping
    public Result<SysUserVO> updateUser(@Valid @RequestBody SysUserDTO sysUserDTO) {
        return Result.success(sysUserService.updateUser(sysUserDTO));
    }

    @Operation(summary = "删除用户", description = "根据用户ID从系统中移除用户。")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteUser(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(sysUserService.deleteUser(id));
    }

    @Operation(summary = "注册用户", description = "用户UI端注册用户。")
    @PostMapping("/register")
    public Result<SysUserVO> register(@Valid @RequestBody SysUserRegisterDTO sysUserRegisterDTO) {
        return Result.success(sysUserService.registerUser(sysUserRegisterDTO));
    }

    @Operation(summary = "内部接口 - 根据用户名获取用户信息", hidden = true)
    @GetMapping("/internal/info/{username}")
    public Result<SysUser> getUserInfoByUsername(@PathVariable("username") String username) {
        SysUser sysUser = sysUserService.selectUserByUsername(username);
        return Result.success(sysUser);
    }
}