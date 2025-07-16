package com.dayz.sapientiacloud_edupivot.system.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "通用分页请求参数")
public class PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6934079705802231364L;

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须从1开始")
    private int pageNum = 1;

    @Schema(description = "每页记录数", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页记录数必须大于0")
    private int pageSize = 10;

    @Schema(description = "起始时间", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}