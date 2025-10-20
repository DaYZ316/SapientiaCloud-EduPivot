package com.dayz.sapientiacloud_edupivot.system.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                .info(new Info().title("SapientiaCloud-EduPivot--system API")
                        .description("智语·云枢：系统服务 API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DaYZ")
                                .email("dyz472734@gmail.com")))
                .components(new Components()
                        .addSecuritySchemes("Authorization", securityScheme));
    }

    @Bean
    public GlobalOperationCustomizer operationCustomizer() {
        return (operation, method) -> {
            operation.addSecurityItem(new SecurityRequirement().addList("Authorization"));
            return operation;
        };
    }
} 