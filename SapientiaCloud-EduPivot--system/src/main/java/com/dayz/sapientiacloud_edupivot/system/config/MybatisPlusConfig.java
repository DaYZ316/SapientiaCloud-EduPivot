package com.dayz.sapientiacloud_edupivot.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public UuidTypeHandler uuidTypeHandler() {
        return new UuidTypeHandler();
    }
     
     // TODO: 可以添加其他MyBatis-Plus配置
     // 例如：
     // - 自定义主键生成器
     // - 自定义类型处理器
     // - 自定义SQL注入器
     // - 元数据处理器配置
} 