package com.dayz.sapientiacloud_edupivot.student.entity.dto;

import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent.AnswerPayload;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Schema(description = "学生课堂练习提交DTO")
public class QuestionStudentSubmitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 120112345987612340L;

    @Schema(description = "题目ID")
    @NotNull(message = "题目ID不能为空")
    private UUID questionId;

    @Schema(description = "作答内容")
    @NotNull(message = "答案不能为空")
    private AnswerPayload answer;
}