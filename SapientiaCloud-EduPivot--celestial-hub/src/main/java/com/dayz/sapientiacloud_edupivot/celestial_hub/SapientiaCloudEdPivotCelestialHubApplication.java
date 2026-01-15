package com.dayz.sapientiacloud_edupivot.celestial_hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableCaching
@EnableConfigurationProperties
@EnableFeignClients
@EnableAsync
@SpringBootApplication
public class SapientiaCloudEdPivotCelestialHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEdPivotCelestialHubApplication.class, args);
    }

}
