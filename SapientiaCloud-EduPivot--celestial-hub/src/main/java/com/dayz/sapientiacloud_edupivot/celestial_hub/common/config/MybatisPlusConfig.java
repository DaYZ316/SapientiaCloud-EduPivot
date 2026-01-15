package com.dayz.sapientiacloud_edupivot.celestial_hub.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public UuidTypeConfig uuidTypeConfig() {
        return new UuidTypeConfig();
    }

    @Bean
    public UuidListTypeHandler uuidListTypeHandler() {
        return new UuidListTypeHandler();
    }

    // 当前配置已满足需求，如需添加其他 MyBatis-Plus 配置，可参考以下示例：
    // - 自定义主键生成器：@Bean public IdentifierGenerator identifierGenerator() { ... }
    // - 自定义类型处理器：已在上面配置 UuidTypeConfig 和 UuidListTypeHandler
    // - 自定义SQL注入器：@Bean public ISqlInjector sqlInjector() { ... }
    // - 元数据处理器配置：@Bean public MetaObjectHandler metaObjectHandler() { ... }
    // - 分页插件：@Bean public MybatisPlusInterceptor mybatisPlusInterceptor() { ... }
    // - 逻辑删除插件：在 application.yml 中配置 mybatis-plus.global-config.db-config.logic-delete-field
} 