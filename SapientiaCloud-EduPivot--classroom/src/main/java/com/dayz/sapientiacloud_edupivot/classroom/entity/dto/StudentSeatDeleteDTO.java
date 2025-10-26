package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "学生座位删除数据传输对象")
public class StudentSeatDeleteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4393508171517823424L;

    @Schema(description = "课程记录ID")
    @NotNull(message = "课程记录ID不能为空")
    private UUID recordId;

    @Schema(description = "学生ID")
    @NotNull(message = "学生ID不能为空")
    private UUID studentId;
}