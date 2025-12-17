package com.dayz.sapientiacloud_edupivot.classroom.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课堂练习发布记录VO")
public class ClassroomQuestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4123482378423146571L;

    @Schema(description = "记录ID")
    private UUID id;

    @Schema(description = "课堂记录ID")
    private UUID classroomId;

    @Schema(description = "题目ID")
    private UUID questionId;

    @Schema(description = "题目标题")
    private String questionTitle;

    @Schema(description = "发布顺序")
    private Integer publishOrder;

    @Schema(description = "是否必答 (0=选答,1=必答)")
    private Integer isRequired;

    @Schema(description = "题目可作答开始时间")
    private LocalDateTime startTime;

    @Schema(description = "题目作答截止时间")
    private LocalDateTime endTime;
}