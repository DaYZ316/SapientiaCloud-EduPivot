package com.dayz.sapientiacloud_edupivot.live.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LiveRoomTokenRequestDTO {
    @Schema(description = "用户角色(0=学生,1=老师,2=助教)")
    @NotNull
    private Integer role;
}