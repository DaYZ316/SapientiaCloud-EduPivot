package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课堂练习发布与更新DTO")
public class ClassroomQuestionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -587230145549820821L;

    @Schema(description = "记录ID（更新时必填）")
    private UUID id;

    @Schema(description = "课堂记录ID")
    @NotNull(message = "课堂ID不能为空")
    private UUID classroomId;

    @Schema(description = "题目ID")
    @NotNull(message = "题目ID不能为空")
    private UUID questionId;

    @Schema(description = "发布顺序")
    @Min(value = 0, message = "发布顺序不能小于0")
    @Max(value = 10000, message = "发布顺序不能大于10000")
    private Integer publishOrder;

    @Schema(description = "题目分值")
    @DecimalMin(value = "0.0", message = "分值不能小于0")
    @DecimalMax(value = "100.0", message = "分值不能大于100")
    private Float score;

    @Schema(description = "是否必答 (0=选答,1=必答)")
    @Min(value = 0, message = "是否必答无效")
    @Max(value = 1, message = "是否必答无效")
    private Integer isRequired;

    @Schema(description = "题目可作答开始时间")
    private LocalDateTime startTime;

    @Schema(description = "题目作答截止时间")
    private LocalDateTime endTime;
}