package com.dayz.sapientiacloud_edupivot.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI springOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name(HttpHeaders.AUTHORIZATION)
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                .info(new Info().title("SapientiaCloud-EduPivot--auth API")
                        .description("智语·云枢：认证服务 API文档")
                        .version("1.0.0")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("DaYZ")
                                .email("dyz472734@gmail.com")));
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            Parameter authorizationHeader = new Parameter()
                    .in("header")
                    .name("Authorization")
                    .description("全局认证Token")
                    .required(false)
                    // 您可以根据需要设置其他属性，如 schema
                    .schema(new io.swagger.v3.oas.models.media.StringSchema());

            operation.addParametersItem(authorizationHeader);
            return operation;
        };
    }
}