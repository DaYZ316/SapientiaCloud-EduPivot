package com.dayz.sapientiacloud_edupivot.student.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;
import java.util.UUID;

@Configuration
public class MongoConfig {

    @Bean
    public Converter<UUID, String> uuidToStringConverter() {
        return new Converter<UUID, String>() {
            @Override
            public String convert(UUID source) {
                if (source == null) {
                    return null;
                }
                return source.toString();
            }
        };
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                uuidToStringConverter()
        ));
    }
}