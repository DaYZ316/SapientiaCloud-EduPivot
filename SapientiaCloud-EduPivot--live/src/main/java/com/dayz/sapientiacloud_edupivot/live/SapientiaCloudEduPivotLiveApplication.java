package com.dayz.sapientiacloud_edupivot.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@EnableConfigurationProperties
@EnableFeignClients
@SpringBootApplication
public class SapientiaCloudEduPivotLiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotLiveApplication.class, args);
    }

}
