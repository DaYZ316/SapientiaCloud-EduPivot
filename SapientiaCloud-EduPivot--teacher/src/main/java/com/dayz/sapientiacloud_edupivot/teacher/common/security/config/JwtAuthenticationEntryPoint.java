package com.dayz.sapientiacloud_edupivot.teacher.common.security.config;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.teacher.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证入口点
 * 处理认证异常，返回401错误
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // 设置响应状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置内容类型
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 设置字符编码
        response.setCharacterEncoding("UTF-8");

        // 创建错误响应
        Result<Void> result = Result.fail(ResultEnum.UNAUTHORIZED);

        // 写入响应
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(result));
        writer.flush();
    }
} 