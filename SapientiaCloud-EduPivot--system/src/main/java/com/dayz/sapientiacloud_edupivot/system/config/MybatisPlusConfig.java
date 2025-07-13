package com.dayz.sapientiacloud_edupivot.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智语·云枢用户服务MyBatis-Plus配置类
 *
 * @author DaYZ
 * @version 1.0.0
 * @since 2025-01-08
 */
@Configuration // Spring配置类注解，标识这是一个配置组件
public class MybatisPlusConfig {

    /**
     * 将自定义的UUID类型处理器注册为Spring Bean
     * MyBatis-Plus的自动配置会自动发现并使用它
     *
     * @return UuidTypeHandler 实例
     */
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