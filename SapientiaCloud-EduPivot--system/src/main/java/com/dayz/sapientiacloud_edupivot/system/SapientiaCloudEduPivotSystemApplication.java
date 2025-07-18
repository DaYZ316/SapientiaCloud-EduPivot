package com.dayz.sapientiacloud_edupivot.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@EnableConfigurationProperties
@SpringBootApplication
public class SapientiaCloudEduPivotSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotSystemApplication.class, args);
    }

}
