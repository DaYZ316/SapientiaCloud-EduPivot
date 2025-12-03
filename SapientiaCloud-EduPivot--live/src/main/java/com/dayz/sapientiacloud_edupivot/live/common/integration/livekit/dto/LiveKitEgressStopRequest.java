package com.dayz.sapientiacloud_edupivot.live.common.integration.livekit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LiveKitEgressStopRequest {

    @JsonProperty("egress_id")
    private String egressId;
}

