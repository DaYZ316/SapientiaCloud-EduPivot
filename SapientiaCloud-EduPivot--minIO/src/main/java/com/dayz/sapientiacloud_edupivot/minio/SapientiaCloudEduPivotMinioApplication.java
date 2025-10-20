package com.dayz.sapientiacloud_edupivot.minio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SapientiaCloudEduPivotMinioApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotMinioApplication.class, args);
    }

} 