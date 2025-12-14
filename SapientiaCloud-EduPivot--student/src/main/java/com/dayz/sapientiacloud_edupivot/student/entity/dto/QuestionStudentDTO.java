package com.dayz.sapientiacloud_edupivot.student.entity.dto;

import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent.AnswerPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "课堂练习作答更新DTO")
public class QuestionStudentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 120112345987612342L;

    @Schema(description = "作答记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "00000000-0000-0000-0000-000000000000")
    @NotNull(message = "作答记录ID不能为空")
    private UUID id;

    @Schema(description = "课堂记录ID")
    @NotNull(message = "课堂记录ID不能为空")
    private UUID classroomId;

    @Schema(description = "学生ID")
    @NotNull(message = "学生ID不能为空")
    private UUID studentId;

    @Schema(description = "题目ID")
    @NotNull(message = "题目ID不能为空")
    private UUID questionId;

    @Schema(description = "作答内容")
    private AnswerPayload answer;

    @Schema(description = "是否正确")
    private Boolean isCorrect;

    @Schema(description = "得分")
    private Float score;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;
}

