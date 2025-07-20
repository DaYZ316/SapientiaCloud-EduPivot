package com.dayz.sapientiacloud_edupivot.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI springOpenApi() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("输入JWT Token，无需添加Bearer前缀");

        return new OpenAPI()
                .info(new Info().title("SapientiaCloud-EduPivot--auth API")
                        .description("智语·云枢：认证服务 API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DaYZ")
                                .email("dyz472734@gmail.com")))
                .components(new Components()
                        .addSecuritySchemes("Authorization", securityScheme));
    }

    @Bean
    public GlobalOperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            // 获取操作ID
            String operationId = operation.getOperationId();
            
            // 获取方法名
            String methodName = null;
            if (handlerMethod != null) {
                methodName = handlerMethod.getMethod().getName();
            }
            
            // 仅对logout和获取用户信息(getUserInfo)接口添加Authorization请求头要求
            if ((methodName != null && (methodName.contains("logout") || methodName.contains("getUserInfo"))) || 
                (operationId != null && (operationId.contains("logout") || operationId.contains("info")))) {
                operation.addSecurityItem(new SecurityRequirement().addList("Authorization"));
            }
            return operation;
        };
    }
}