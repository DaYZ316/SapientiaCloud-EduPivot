package com.dayz.sapientiacloud_edupivot.course.common.config;

import com.dayz.sapientiacloud_edupivot.course.interceptor.CourseAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Lazy
    private final CourseAccessInterceptor courseAccessInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册课程访问拦截器，拦截所有Controller接口
        registry.addInterceptor(courseAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 排除公开接口
                        "/public/**",
                        // 排除Swagger相关接口
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        // 排除健康检查等系统接口
                        "/actuator/**"
                );
    }
}