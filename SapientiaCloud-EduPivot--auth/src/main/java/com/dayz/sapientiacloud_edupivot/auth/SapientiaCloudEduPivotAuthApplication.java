package com.dayz.sapientiacloud_edupivot.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SapientiaCloudEduPivotAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotAuthApplication.class, args);
    }

}
