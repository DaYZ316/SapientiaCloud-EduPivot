package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LiveKitEgressStartResponse {

    @JsonProperty("egress_id")
    private String egressId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("room_name")
    private String roomName;
}

