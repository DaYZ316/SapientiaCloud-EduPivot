package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LiveKitEgressFileOutput {

    /**
     * 文件类型，如 MP4
     */
    @JsonProperty("file_type")
    private String fileType;

    /**
     * 存储路径（S3 对象键）
     */
    @JsonProperty("filepath")
    private String filepath;

    /**
     * S3 上传配置
     */
    @JsonProperty("s3")
    private S3UploadConfig s3;

    @Data
    public static class S3UploadConfig {
        @JsonProperty("access_key")
        private String accessKey;
        @JsonProperty("secret")
        private String secret;
        @JsonProperty("region")
        private String region;
        @JsonProperty("endpoint")
        private String endpoint;
        @JsonProperty("bucket")
        private String bucket;
        @JsonProperty("force_path_style")
        private Boolean forcePathStyle;
    }
}

