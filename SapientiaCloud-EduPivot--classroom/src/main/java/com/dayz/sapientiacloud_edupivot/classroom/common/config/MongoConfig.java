package com.dayz.sapientiacloud_edupivot.classroom.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;
import java.util.UUID;

/**
 * MongoDB配置类
 * 配置UUID的序列化和反序列化，特别支持UUIDv7
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Configuration
public class MongoConfig {


    /**
     * UUID转字符串转换器
     * 将UUID（包括UUIDv7）转换为字符串格式存储
     */
    @Bean
    public Converter<UUID, String> uuidToStringConverter() {
        return new Converter<UUID, String>() {
            @Override
            public String convert(UUID source) {
                if (source == null) {
                    return null;
                }
                String uuidString = source.toString();
                return uuidString;
            }
        };
    }


    /**
     * 自定义MongoDB转换器
     * 只注册UUID转String的转换器，避免影响其他String字段
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                uuidToStringConverter()
        ));
    }
}
