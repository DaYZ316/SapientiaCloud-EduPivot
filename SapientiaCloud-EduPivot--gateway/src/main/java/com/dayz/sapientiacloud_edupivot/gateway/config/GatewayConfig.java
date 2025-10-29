package com.dayz.sapientiacloud_edupivot.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("github_login_route", r -> r
                        .path("/github/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://SapientiaCloud-EduPivot--auth")
                )
                .route("api_github_login_route", r -> r
                        .path("/api/auth/github/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://SapientiaCloud-EduPivot--auth")
                )
                .route("oauth2_route", r -> r
                        .path("/oauth2/**")
                        .uri("lb://SapientiaCloud-EduPivot--auth")
                )
                .route("login_oauth2_route", r -> r
                        .path("/login/oauth2/**")
                        .uri("lb://SapientiaCloud-EduPivot--auth")
                )
                .build();
    }
}