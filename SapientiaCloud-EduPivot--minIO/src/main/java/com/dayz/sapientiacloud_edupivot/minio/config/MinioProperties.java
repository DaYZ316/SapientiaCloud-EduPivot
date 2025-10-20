package com.dayz.sapientiacloud_edupivot.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * minio 配置类
 *
 * @author LANDH
 */
@Component
@ConfigurationProperties(prefix = "minio.config")
@Data
public class MinioProperties {

    private String ip;

    private int port;

    private String accessKey;

    private String secretKey;

    private String bucketName;
} 