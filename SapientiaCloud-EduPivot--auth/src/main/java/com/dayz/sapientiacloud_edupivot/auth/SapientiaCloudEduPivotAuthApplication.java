package com.dayz.sapientiacloud_edupivot.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.dayz.sapientiacloud_edupivot.auth")
@SpringBootApplication(
        scanBasePackages = "com.dayz.sapientiacloud_edupivot"
)
public class SapientiaCloudEduPivotAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotAuthApplication.class, args);
    }

}
