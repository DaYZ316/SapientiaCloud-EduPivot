package com.dayz.sapientiacloud_edupivot.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import reactor.core.publisher.Hooks;

@EnableDiscoveryClient
@SpringBootApplication
public class SapientiaCloudEduPivotGatewayApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(SapientiaCloudEduPivotGatewayApplication.class, args);
    }

}
