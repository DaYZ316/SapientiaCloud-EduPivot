package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LiveKitEgressStartRequest {

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("layout")
    private String layout;

    @JsonProperty("file_outputs")
    private List<LiveKitEgressFileOutput> fileOutputs;
}

