package com.dayz.sapientiacloud_edupivot.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SapientiaCloudEduPivotGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapientiaCloudEduPivotGatewayApplication.class, args);
    }

}
