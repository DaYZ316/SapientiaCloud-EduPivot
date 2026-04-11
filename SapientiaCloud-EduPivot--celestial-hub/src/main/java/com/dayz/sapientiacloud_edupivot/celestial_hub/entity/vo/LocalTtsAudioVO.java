package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalTtsAudioVO {

    private byte[] audioBytes;

    private Integer durationMs;
}
