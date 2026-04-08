package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "本地TTS请求")
public class LocalTtsRequestDTO {

    @Schema(description = "任务ID")
    @JsonProperty("taskId")
    private String taskId;

    @Schema(description = "待合成文本")
    private String text;

    @Schema(description = "音色编码")
    @JsonProperty("voiceCode")
    private String voiceCode;

    @Schema(description = "音频格式")
    @JsonProperty("audioFormat")
    private String audioFormat;
}
