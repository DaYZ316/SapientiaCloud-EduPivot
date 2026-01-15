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
     * 存储路径
     */
    @JsonProperty("filepath")
    private String filepath;
}

