package com.dayz.sapientiacloud_edupivot.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI springOpenApi() {
        return new OpenAPI()
                .info(new Info().title("SapientiaCloud-EduPivot--auth API")
                        .description("智语·云枢：认证服务 API文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("DaYZ")
                                .email("dyz472734@gmail.com")));
    }
}