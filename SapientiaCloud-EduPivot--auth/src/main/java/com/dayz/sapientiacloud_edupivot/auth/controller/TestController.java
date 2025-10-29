package com.dayz.sapientiacloud_edupivot.auth.controller;

import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 * 用于验证网关白名单配置是否生效
 */
@RestController
public class TestController {

    /**
     * 测试端点 - 无需认证
     * 用于验证网关白名单配置是否正确工作
     */
    @GetMapping("/test/public")
    public Result<String> publicTest() {
        return Result.success("公共测试端点访问成功");
    }
    
    /**
     * GitHub登录入口测试 - 简化版
     */
    @GetMapping("/test/github/login")
    public Result<String> githubLoginTest() {
        return Result.success("GitHub登录路径测试成功");
    }
    
    /**
     * API版GitHub登录入口测试
     * 直接映射到目标路径，验证路由是否正确
     */
    @GetMapping("/api/auth/github/test")
    public Result<String> apiGithubLoginTest() {
        return Result.success("API版GitHub登录路径测试成功");
    }
}