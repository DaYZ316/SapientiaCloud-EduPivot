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


    @JsonProperty("await_start_signal")
    private Boolean awaitStartSignal;


    @JsonProperty("wait_for_track")
    private Boolean waitForTrack;


    @JsonProperty("token")
    private String token;

    @JsonProperty("file_outputs")
    private List<LiveKitEgressFileOutput> fileOutputs;
}

