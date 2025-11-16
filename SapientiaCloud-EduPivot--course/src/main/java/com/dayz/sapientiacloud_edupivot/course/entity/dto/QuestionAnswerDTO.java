package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "填空简答数据传输对象")
public class QuestionAnswerDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 200141681992066454L;

    @Schema(name = "id", description = "答案ID")
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    private UUID questionId;

    @Schema(name = "answerContent", description = "本空答案")
    private String answerContent;

    @Schema(name = "explanation", description = "本空解析")
    private String explanation;

    @Schema(name = "score", description = "分数")
    private BigDecimal score;

    @Schema(name = "sortOrder", description = "本空序号")
    private Integer sortOrder;
}
